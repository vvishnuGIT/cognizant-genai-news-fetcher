package com.cognizant.genai.newsfetcher.controller;

import com.cognizant.genai.newsfetcher.model.NewsSource;
import com.cognizant.genai.newsfetcher.service.NewsSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NewsSourceController {
    
    private final NewsSourceService newsSourceService;
    
    @GetMapping
    public ResponseEntity<List<NewsSource>> getAllSources() {
        try {
            List<NewsSource> sources = newsSourceService.getAllSources();
            return ResponseEntity.ok(sources);
            
        } catch (Exception e) {
            log.error("Error fetching news sources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<NewsSource>> getActiveSources() {
        try {
            List<NewsSource> sources = newsSourceService.getActiveSources();
            return ResponseEntity.ok(sources);
            
        } catch (Exception e) {
            log.error("Error fetching active news sources: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NewsSource> getSourceById(@PathVariable Long id) {
        try {
            Optional<NewsSource> source = newsSourceService.getSourceById(id);
            
            if (source.isPresent()) {
                return ResponseEntity.ok(source.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error fetching news source {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/by-name/{name}")
    public ResponseEntity<NewsSource> getSourceByName(@PathVariable String name) {
        try {
            Optional<NewsSource> source = newsSourceService.getSourceByName(name);
            
            if (source.isPresent()) {
                return ResponseEntity.ok(source.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error fetching news source by name {}: {}", name, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createSource(@Valid @RequestBody NewsSource source) {
        try {
            NewsSource createdSource = newsSourceService.createSource(source);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSource);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            log.error("Error creating news source: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to create news source: " + e.getMessage()
                ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSource(@PathVariable Long id, @Valid @RequestBody NewsSource source) {
        try {
            NewsSource updatedSource = newsSourceService.updateSource(id, source);
            return ResponseEntity.ok(updatedSource);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            log.error("Error updating news source {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to update news source: " + e.getMessage()
                ));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSource(@PathVariable Long id) {
        try {
            newsSourceService.deleteSource(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "News source deleted successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            log.error("Error deleting news source {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to delete news source: " + e.getMessage()
                ));
        }
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateSource(@PathVariable Long id) {
        try {
            newsSourceService.activateSource(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "News source activated successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error activating news source {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to activate news source: " + e.getMessage()
                ));
        }
    }
    
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateSource(@PathVariable Long id) {
        try {
            newsSourceService.deactivateSource(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "News source deactivated successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error deactivating news source {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to deactivate news source: " + e.getMessage()
                ));
        }
    }
    
    @GetMapping("/due-for-fetch")
    public ResponseEntity<List<NewsSource>> getSourcesDueForFetch() {
        try {
            List<NewsSource> sources = newsSourceService.getSourcesDueForFetch();
            return ResponseEntity.ok(sources);
            
        } catch (Exception e) {
            log.error("Error fetching sources due for fetch: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{id}/update-fetch-time")
    public ResponseEntity<Map<String, String>> updateLastFetchTime(@PathVariable Long id) {
        try {
            newsSourceService.updateLastFetchTime(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Last fetch time updated successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error updating last fetch time for source {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to update last fetch time: " + e.getMessage()
                ));
        }
    }
}