package com.cognizant.genai.newsfetcher.service;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import com.microsoft.graph.models.ListItem;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ListItemCollectionPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharePointService {
    
    private final GraphServiceClient graphServiceClient;
    private final ContentAnalysisService contentAnalysisService;
    
    @Value("${azure.sharepoint.site-url}")
    private String siteUrl;
    
    @Value("${azure.sharepoint.list-name}")
    private String listName;
    
    public List<NewsArticle> fetchArticlesFromSharePoint(Set<String> keywords) {
        List<NewsArticle> articles = new ArrayList<>();
        
        if (graphServiceClient == null) {
            log.warn("Graph Service Client not available. Skipping SharePoint fetch.");
            return articles;
        }
        
        try {
            log.info("Fetching articles from SharePoint site: {}", siteUrl);
            
            // Get the site
            Site site = getSiteByUrl(siteUrl);
            if (site == null) {
                log.error("Could not find SharePoint site: {}", siteUrl);
                return articles;
            }
            
            // Get the list
            var lists = graphServiceClient.sites(site.id).lists().buildRequest().get();
            var targetList = lists.getCurrentPage().stream()
                .filter(list -> list.displayName.equals(listName))
                .findFirst();
            
            if (targetList.isEmpty()) {
                log.error("Could not find SharePoint list: {}", listName);
                return articles;
            }
            
            // Get list items
            ListItemCollectionPage listItems = graphServiceClient
                .sites(site.id)
                .lists(targetList.get().id)
                .items()
                .buildRequest()
                .expand("fields")
                .get();
            
            log.info("Found {} items in SharePoint list", listItems.getCurrentPage().size());
            
            for (ListItem item : listItems.getCurrentPage()) {
                try {
                    NewsArticle article = convertSharePointItemToArticle(item, keywords);
                    if (article != null) {
                        articles.add(article);
                    }
                } catch (Exception e) {
                    log.warn("Failed to process SharePoint item: {}", e.getMessage());
                }
            }
            
            log.info("Successfully processed {} articles from SharePoint", articles.size());
            
        } catch (Exception e) {
            log.error("Failed to fetch articles from SharePoint: {}", e.getMessage());
        }
        
        return articles;
    }
    
    private Site getSiteByUrl(String siteUrl) {
        try {
            // Extract site path from URL
            String sitePath = siteUrl.replace("https://", "").replace("http://", "");
            String[] parts = sitePath.split("/");
            
            if (parts.length < 2) {
                log.error("Invalid SharePoint site URL format: {}", siteUrl);
                return null;
            }
            
            String hostname = parts[0];
            String siteName = parts[parts.length - 1];
            
            return graphServiceClient.sites(hostname + ":/" + siteName).buildRequest().get();
            
        } catch (Exception e) {
            log.error("Failed to get SharePoint site: {}", e.getMessage());
            return null;
        }
    }
    
    private NewsArticle convertSharePointItemToArticle(ListItem item, Set<String> keywords) {
        try {
            if (item.fields == null || item.fields.additionalDataManager() == null) {
                log.debug("SharePoint item has no fields data");
                return null;
            }
            
            var fields = item.fields.additionalDataManager();
            
            NewsArticle article = new NewsArticle();
            
            // Extract basic fields (adjust field names based on your SharePoint list structure)
            String title = getFieldValue(fields, "Title");
            String description = getFieldValue(fields, "Description");
            String content = getFieldValue(fields, "Content");
            String url = getFieldValue(fields, "URL");
            String author = getFieldValue(fields, "Author");
            String publishedDateStr = getFieldValue(fields, "PublishedDate");
            
            if (title == null || title.trim().isEmpty()) {
                log.debug("SharePoint item missing title, skipping");
                return null;
            }
            
            article.setTitle(title);
            article.setDescription(description);
            article.setContent(content);
            article.setUrl(url != null ? url : generateSharePointItemUrl(item));
            article.setAuthor(author);
            article.setSourceName("Cognizant SharePoint");
            article.setSourceType(NewsArticle.SourceType.SHAREPOINT);
            article.setIsFromSharepoint(true);
            article.setSharepointItemId(item.id);
            
            // Parse published date
            if (publishedDateStr != null) {
                try {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(publishedDateStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    article.setPublishedDate(zonedDateTime.toLocalDateTime());
                } catch (Exception e) {
                    log.debug("Could not parse published date: {}", publishedDateStr);
                    article.setPublishedDate(LocalDateTime.now());
                }
            } else {
                article.setPublishedDate(LocalDateTime.now());
            }
            
            // Analyze content for relevance and sentiment
            String contentToAnalyze = title + " " + (description != null ? description : "") + " " + (content != null ? content : "");
            
            double relevanceScore = contentAnalysisService.calculateRelevanceScore(contentToAnalyze, keywords);
            article.setRelevanceScore(relevanceScore);
            
            // Only process articles with minimum relevance
            if (relevanceScore < 0.3) {
                log.debug("Skipping SharePoint article with low relevance score: {}", title);
                return null;
            }
            
            double sentimentScore = contentAnalysisService.analyzeSentiment(contentToAnalyze);
            article.setSentimentScore(sentimentScore);
            
            Set<String> extractedTags = contentAnalysisService.extractTags(contentToAnalyze, keywords);
            article.setTags(extractedTags);
            
            return article;
            
        } catch (Exception e) {
            log.warn("Failed to convert SharePoint item to NewsArticle: {}", e.getMessage());
            return null;
        }
    }
    
    private String getFieldValue(Object fields, String fieldName) {
        try {
            if (fields instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> fieldMap = (java.util.Map<String, Object>) fields;
                Object value = fieldMap.get(fieldName);
                return value != null ? value.toString() : null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String generateSharePointItemUrl(ListItem item) {
        return siteUrl + "/Lists/" + listName + "/DispForm.aspx?ID=" + item.id;
    }
}