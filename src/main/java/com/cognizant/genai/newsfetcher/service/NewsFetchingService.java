package com.cognizant.genai.newsfetcher.service;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import com.cognizant.genai.newsfetcher.model.NewsSource;
import com.cognizant.genai.newsfetcher.repository.NewsArticleRepository;
import com.cognizant.genai.newsfetcher.repository.NewsSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsFetchingService {
    
    private final NewsArticleRepository newsArticleRepository;
    private final NewsSourceRepository newsSourceRepository;
    private final RssFeedService rssFeedService;
    private final SharePointService sharePointService;
    private final NewsSourceService newsSourceService;
    
    @Value("${news.keywords}")
    private List<String> keywordsList;
    
    @Scheduled(cron = "${news.scheduling.fetch-interval}")
    public void scheduledNewsFetch() {
        log.info("Starting scheduled news fetch");
        fetchAllNews();
    }
    
    @Scheduled(cron = "${news.scheduling.cleanup-interval}")
    public void scheduledCleanup() {
        log.info("Starting scheduled cleanup of old news articles");
        cleanupOldArticles();
    }
    
    public void fetchAllNews() {
        Set<String> keywords = new HashSet<>(keywordsList);
        
        try {
            log.info("Starting news fetch with {} keywords", keywords.size());
            
            // Fetch from external RSS sources
            CompletableFuture<List<NewsArticle>> rssArticlesFuture = fetchFromRssSources(keywords);
            
            // Fetch from SharePoint
            CompletableFuture<List<NewsArticle>> sharePointArticlesFuture = fetchFromSharePoint(keywords);
            
            // Wait for both operations to complete
            CompletableFuture.allOf(rssArticlesFuture, sharePointArticlesFuture).join();
            
            List<NewsArticle> rssArticles = rssArticlesFuture.join();
            List<NewsArticle> sharePointArticles = sharePointArticlesFuture.join();
            
            // Combine and deduplicate articles
            List<NewsArticle> allArticles = new ArrayList<>();
            allArticles.addAll(rssArticles);
            allArticles.addAll(sharePointArticles);
            
            List<NewsArticle> deduplicatedArticles = deduplicateArticles(allArticles);
            
            // Save new articles
            int savedCount = saveNewArticles(deduplicatedArticles);
            
            log.info("News fetch completed. Processed {} articles, saved {} new articles", 
                     allArticles.size(), savedCount);
            
        } catch (Exception e) {
            log.error("Error during news fetch: {}", e.getMessage(), e);
        }
    }
    
    @Async("newsTaskExecutor")
    public CompletableFuture<List<NewsArticle>> fetchFromRssSources(Set<String> keywords) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            List<NewsSource> rssSources = newsSourceRepository.findByIsActiveAndSourceType(
                true, NewsArticle.SourceType.RSS);
            
            log.info("Fetching from {} RSS sources", rssSources.size());
            
            for (NewsSource source : rssSources) {
                try {
                    List<NewsArticle> sourceArticles = rssFeedService.fetchArticlesFromRss(
                        source.getUrl(), source.getName(), keywords);
                    articles.addAll(sourceArticles);
                    
                    // Update last fetched time
                    source.setLastFetched(LocalDateTime.now());
                    newsSourceRepository.save(source);
                    
                } catch (Exception e) {
                    log.error("Failed to fetch from RSS source {}: {}", source.getName(), e.getMessage());
                }
            }
            
            log.info("Fetched {} articles from RSS sources", articles.size());
            
        } catch (Exception e) {
            log.error("Error fetching from RSS sources: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(articles);
    }
    
    @Async("newsTaskExecutor")
    public CompletableFuture<List<NewsArticle>> fetchFromSharePoint(Set<String> keywords) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            log.info("Fetching from SharePoint");
            articles = sharePointService.fetchArticlesFromSharePoint(keywords);
            log.info("Fetched {} articles from SharePoint", articles.size());
            
        } catch (Exception e) {
            log.error("Error fetching from SharePoint: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(articles);
    }
    
    private List<NewsArticle> deduplicateArticles(List<NewsArticle> articles) {
        Map<String, NewsArticle> uniqueArticles = new LinkedHashMap<>();
        
        for (NewsArticle article : articles) {
            String key = generateDeduplicationKey(article);
            
            if (!uniqueArticles.containsKey(key)) {
                uniqueArticles.put(key, article);
            } else {
                // Keep the article with higher relevance score
                NewsArticle existing = uniqueArticles.get(key);
                if (article.getRelevanceScore() != null && existing.getRelevanceScore() != null) {
                    if (article.getRelevanceScore() > existing.getRelevanceScore()) {
                        uniqueArticles.put(key, article);
                    }
                }
            }
        }
        
        log.info("Deduplicated {} articles down to {} unique articles", 
                 articles.size(), uniqueArticles.size());
        
        return new ArrayList<>(uniqueArticles.values());
    }
    
    private String generateDeduplicationKey(NewsArticle article) {
        // Use URL as primary key, fall back to normalized title
        if (article.getUrl() != null && !article.getUrl().trim().isEmpty()) {
            return article.getUrl().toLowerCase().trim();
        }
        
        if (article.getTitle() != null && !article.getTitle().trim().isEmpty()) {
            return article.getTitle().toLowerCase().trim().replaceAll("\\s+", " ");
        }
        
        return UUID.randomUUID().toString(); // Fallback for articles without URL or title
    }
    
    @Transactional
    public int saveNewArticles(List<NewsArticle> articles) {
        int savedCount = 0;
        
        for (NewsArticle article : articles) {
            try {
                // Check if article already exists
                if (article.getUrl() != null && newsArticleRepository.existsByUrl(article.getUrl())) {
                    log.debug("Article already exists: {}", article.getUrl());
                    continue;
                }
                
                if (article.getSharepointItemId() != null && 
                    newsArticleRepository.findBySharepointItemId(article.getSharepointItemId()).isPresent()) {
                    log.debug("SharePoint article already exists: {}", article.getSharepointItemId());
                    continue;
                }
                
                newsArticleRepository.save(article);
                savedCount++;
                log.debug("Saved new article: {}", article.getTitle());
                
            } catch (Exception e) {
                log.warn("Failed to save article '{}': {}", article.getTitle(), e.getMessage());
            }
        }
        
        return savedCount;
    }
    
    @Transactional
    public void cleanupOldArticles() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Keep articles for 30 days
            
            long beforeCount = newsArticleRepository.count();
            newsArticleRepository.deleteByCreatedAtBefore(cutoffDate);
            long afterCount = newsArticleRepository.count();
            
            log.info("Cleaned up {} old articles (before: {}, after: {})", 
                     beforeCount - afterCount, beforeCount, afterCount);
            
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage());
        }
    }
    
    public List<NewsArticle> searchArticles(String query, int page, int size) {
        // This is a simple search implementation
        // In a production system, you might want to use Elasticsearch or similar
        
        if (query == null || query.trim().isEmpty()) {
            return newsArticleRepository.findAll().stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        }
        
        return newsArticleRepository.findByKeyword(query, 
            org.springframework.data.domain.PageRequest.of(page, size))
            .getContent();
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Basic counts
            stats.put("totalArticles", newsArticleRepository.count());
            stats.put("sharepointArticles", newsArticleRepository.findByIsFromSharepoint(true).size());
            stats.put("externalArticles", newsArticleRepository.findByIsFromSharepoint(false).size());
            
            // Recent articles (last 7 days)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            stats.put("recentArticles", newsArticleRepository.countArticlesSince(weekAgo));
            
            // Source breakdown
            List<Object[]> sourceStats = newsArticleRepository.getArticleCountBySource();
            Map<String, Long> sourceBreakdown = new HashMap<>();
            for (Object[] stat : sourceStats) {
                sourceBreakdown.put((String) stat[0], (Long) stat[1]);
            }
            stats.put("sourceBreakdown", sourceBreakdown);
            
            // Average relevance score
            List<NewsArticle> allArticles = newsArticleRepository.findAll();
            double avgRelevance = allArticles.stream()
                .filter(a -> a.getRelevanceScore() != null)
                .mapToDouble(NewsArticle::getRelevanceScore)
                .average()
                .orElse(0.0);
            stats.put("averageRelevanceScore", avgRelevance);
            
        } catch (Exception e) {
            log.error("Error generating statistics: {}", e.getMessage());
        }
        
        return stats;
    }
}