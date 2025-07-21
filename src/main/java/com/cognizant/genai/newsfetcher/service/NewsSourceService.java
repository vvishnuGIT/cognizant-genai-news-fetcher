package com.cognizant.genai.newsfetcher.service;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import com.cognizant.genai.newsfetcher.model.NewsSource;
import com.cognizant.genai.newsfetcher.repository.NewsSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsSourceService {
    
    private final NewsSourceRepository newsSourceRepository;
    
    @Value("${news.sources.external:}")
    private String externalSourcesConfig;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultSources() {
        log.info("Initializing default news sources");
        
        try {
            // Create default RSS sources
            initializeDefaultRssSources();
            log.info("Default news sources initialization completed");
            
        } catch (Exception e) {
            log.error("Error initializing default news sources: {}", e.getMessage());
        }
    }
    
    private void initializeDefaultRssSources() {
        // Default RSS sources for GenAI news
        String[][] defaultSources = {
            {"TechCrunch AI", "https://techcrunch.com/category/artificial-intelligence/feed/", "RSS"},
            {"MIT Technology Review AI", "https://www.technologyreview.com/topic/artificial-intelligence/feed/", "RSS"},
            {"AI News", "https://www.artificialintelligence-news.com/feed/", "RSS"},
            {"OpenAI Blog", "https://openai.com/blog/rss.xml", "RSS"},
            {"Google AI Blog", "https://ai.googleblog.com/feeds/posts/default", "RSS"}
        };
        
        for (String[] sourceData : defaultSources) {
            String name = sourceData[0];
            String url = sourceData[1];
            String type = sourceData[2];
            
            if (!newsSourceRepository.existsByName(name)) {
                NewsSource source = new NewsSource();
                source.setName(name);
                source.setUrl(url);
                source.setSourceType(NewsArticle.SourceType.valueOf(type.toUpperCase()));
                source.setIsActive(true);
                source.setDescription("Auto-configured external RSS source for GenAI news");
                source.setFetchFrequencyHours(6);
                
                newsSourceRepository.save(source);
                log.info("Added default news source: {}", name);
            }
        }
    }
    
    public List<NewsSource> getAllSources() {
        return newsSourceRepository.findAll();
    }
    
    public List<NewsSource> getActiveSources() {
        return newsSourceRepository.findByIsActive(true);
    }
    
    public Optional<NewsSource> getSourceById(Long id) {
        return newsSourceRepository.findById(id);
    }
    
    public Optional<NewsSource> getSourceByName(String name) {
        return newsSourceRepository.findByName(name);
    }
    
    public NewsSource createSource(NewsSource source) {
        if (newsSourceRepository.existsByName(source.getName())) {
            throw new IllegalArgumentException("News source with name '" + source.getName() + "' already exists");
        }
        
        if (newsSourceRepository.existsByUrl(source.getUrl())) {
            throw new IllegalArgumentException("News source with URL '" + source.getUrl() + "' already exists");
        }
        
        return newsSourceRepository.save(source);
    }
    
    public NewsSource updateSource(Long id, NewsSource updatedSource) {
        Optional<NewsSource> existingSource = newsSourceRepository.findById(id);
        
        if (existingSource.isEmpty()) {
            throw new IllegalArgumentException("News source with ID " + id + " not found");
        }
        
        NewsSource source = existingSource.get();
        
        // Check for name conflicts (excluding current source)
        if (!source.getName().equals(updatedSource.getName()) && 
            newsSourceRepository.existsByName(updatedSource.getName())) {
            throw new IllegalArgumentException("News source with name '" + updatedSource.getName() + "' already exists");
        }
        
        // Check for URL conflicts (excluding current source)
        if (!source.getUrl().equals(updatedSource.getUrl()) && 
            newsSourceRepository.existsByUrl(updatedSource.getUrl())) {
            throw new IllegalArgumentException("News source with URL '" + updatedSource.getUrl() + "' already exists");
        }
        
        // Update fields
        source.setName(updatedSource.getName());
        source.setUrl(updatedSource.getUrl());
        source.setSourceType(updatedSource.getSourceType());
        source.setIsActive(updatedSource.getIsActive());
        source.setDescription(updatedSource.getDescription());
        source.setFetchFrequencyHours(updatedSource.getFetchFrequencyHours());
        source.setHeaders(updatedSource.getHeaders());
        source.setAuthenticationRequired(updatedSource.getAuthenticationRequired());
        
        return newsSourceRepository.save(source);
    }
    
    public void deleteSource(Long id) {
        if (!newsSourceRepository.existsById(id)) {
            throw new IllegalArgumentException("News source with ID " + id + " not found");
        }
        
        newsSourceRepository.deleteById(id);
    }
    
    public void activateSource(Long id) {
        Optional<NewsSource> source = newsSourceRepository.findById(id);
        if (source.isPresent()) {
            source.get().setIsActive(true);
            newsSourceRepository.save(source.get());
        }
    }
    
    public void deactivateSource(Long id) {
        Optional<NewsSource> source = newsSourceRepository.findById(id);
        if (source.isPresent()) {
            source.get().setIsActive(false);
            newsSourceRepository.save(source.get());
        }
    }
    
    public List<NewsSource> getSourcesDueForFetch() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(6); // Default fetch frequency
        return newsSourceRepository.findSourcesDueForFetch(cutoffTime);
    }
    
    public void updateLastFetchTime(Long sourceId) {
        Optional<NewsSource> source = newsSourceRepository.findById(sourceId);
        if (source.isPresent()) {
            source.get().setLastFetched(LocalDateTime.now());
            newsSourceRepository.save(source.get());
        }
    }
}