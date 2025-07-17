package com.cognizant.genai.newsfetcher.service;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssFeedService {
    
    private final ContentAnalysisService contentAnalysisService;
    
    public List<NewsArticle> fetchArticlesFromRss(String feedUrl, String sourceName, Set<String> keywords) {
        List<NewsArticle> articles = new ArrayList<>();
        
        try {
            log.info("Fetching RSS feed from: {}", feedUrl);
            
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));
            
            log.info("Found {} entries in RSS feed from {}", feed.getEntries().size(), sourceName);
            
            for (SyndEntry entry : feed.getEntries()) {
                try {
                    NewsArticle article = convertToNewsArticle(entry, sourceName, keywords);
                    if (article != null) {
                        articles.add(article);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process RSS entry: {}", e.getMessage());
                }
            }
            
            log.info("Successfully processed {} articles from {}", articles.size(), sourceName);
            
        } catch (Exception e) {
            log.error("Failed to fetch RSS feed from {}: {}", feedUrl, e.getMessage());
        }
        
        return articles;
    }
    
    private NewsArticle convertToNewsArticle(SyndEntry entry, String sourceName, Set<String> keywords) {
        try {
            NewsArticle article = new NewsArticle();
            
            // Basic information
            article.setTitle(entry.getTitle());
            article.setUrl(entry.getLink());
            article.setSourceName(sourceName);
            article.setSourceType(NewsArticle.SourceType.RSS);
            article.setIsFromSharepoint(false);
            
            // Description/Content
            if (entry.getDescription() != null) {
                article.setDescription(entry.getDescription().getValue());
                article.setContent(entry.getDescription().getValue());
            }
            
            // Author
            if (entry.getAuthor() != null && !entry.getAuthor().isEmpty()) {
                article.setAuthor(entry.getAuthor());
            }
            
            // Published date
            if (entry.getPublishedDate() != null) {
                article.setPublishedDate(
                    entry.getPublishedDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                );
            } else {
                article.setPublishedDate(LocalDateTime.now());
            }
            
            // Analyze content for relevance and sentiment
            if (article.getTitle() != null && article.getDescription() != null) {
                double relevanceScore = contentAnalysisService.calculateRelevanceScore(
                    article.getTitle() + " " + article.getDescription(), keywords);
                article.setRelevanceScore(relevanceScore);
                
                // Only process articles with minimum relevance
                if (relevanceScore < 0.3) {
                    log.debug("Skipping article with low relevance score: {}", article.getTitle());
                    return null;
                }
                
                double sentimentScore = contentAnalysisService.analyzeSentiment(
                    article.getTitle() + " " + article.getDescription());
                article.setSentimentScore(sentimentScore);
                
                Set<String> extractedTags = contentAnalysisService.extractTags(
                    article.getTitle() + " " + article.getDescription(), keywords);
                article.setTags(extractedTags);
            }
            
            return article;
            
        } catch (Exception e) {
            log.warn("Failed to convert RSS entry to NewsArticle: {}", e.getMessage());
            return null;
        }
    }
}