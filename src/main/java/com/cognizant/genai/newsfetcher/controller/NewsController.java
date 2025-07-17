package com.cognizant.genai.newsfetcher.controller;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import com.cognizant.genai.newsfetcher.repository.NewsArticleRepository;
import com.cognizant.genai.newsfetcher.service.NewsFetchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NewsController {
    
    private final NewsArticleRepository newsArticleRepository;
    private final NewsFetchingService newsFetchingService;
    
    @GetMapping
    public ResponseEntity<Page<NewsArticle>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<NewsArticle> articles = newsArticleRepository.findAll(pageable);
            
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NewsArticle> getArticleById(@PathVariable Long id) {
        try {
            Optional<NewsArticle> article = newsArticleRepository.findById(id);
            
            if (article.isPresent()) {
                return ResponseEntity.ok(article.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error fetching article {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<NewsArticle>> searchArticles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            List<NewsArticle> articles = newsFetchingService.searchArticles(query, page, size);
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error searching articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<NewsArticle>> getRecentArticles(
            @RequestParam(defaultValue = "7") int days) {
        
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<NewsArticle> articles = newsArticleRepository.findRecentArticles(since);
            
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching recent articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/by-source/{sourceName}")
    public ResponseEntity<List<NewsArticle>> getArticlesBySource(@PathVariable String sourceName) {
        try {
            List<NewsArticle> articles = newsArticleRepository.findBySourceName(sourceName);
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching articles by source {}: {}", sourceName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/sharepoint")
    public ResponseEntity<List<NewsArticle>> getSharePointArticles() {
        try {
            List<NewsArticle> articles = newsArticleRepository.findByIsFromSharepoint(true);
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching SharePoint articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/external")
    public ResponseEntity<List<NewsArticle>> getExternalArticles() {
        try {
            List<NewsArticle> articles = newsArticleRepository.findByIsFromSharepoint(false);
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching external articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/relevant")
    public ResponseEntity<Page<NewsArticle>> getRelevantArticles(
            @RequestParam(defaultValue = "0.5") double minRelevanceScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("relevanceScore").descending());
            
            Page<NewsArticle> articles = newsArticleRepository
                .findByRelevanceScoreGreaterThanEqual(minRelevanceScore, pageable);
            
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching relevant articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<Page<NewsArticle>> getArticlesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by("publishedDate").descending());
            
            Page<NewsArticle> articles = newsArticleRepository
                .findByPublishedDateBetween(start, end, pageable);
            
            return ResponseEntity.ok(articles);
            
        } catch (Exception e) {
            log.error("Error fetching articles by date range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/fetch")
    public ResponseEntity<Map<String, String>> triggerManualFetch() {
        try {
            log.info("Manual news fetch triggered via API");
            newsFetchingService.fetchAllNews();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "News fetch initiated successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error triggering manual fetch: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to initiate news fetch: " + e.getMessage()
                ));
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = newsFetchingService.getStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error fetching statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteArticle(@PathVariable Long id) {
        try {
            if (newsArticleRepository.existsById(id)) {
                newsArticleRepository.deleteById(id);
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Article deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting article {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to delete article: " + e.getMessage()
                ));
        }
    }
}