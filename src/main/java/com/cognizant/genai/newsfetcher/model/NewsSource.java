package com.cognizant.genai.newsfetcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NewsSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private NewsArticle.SourceType sourceType;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_fetched")
    private LocalDateTime lastFetched;
    
    @Column(name = "fetch_frequency_hours")
    private Integer fetchFrequencyHours = 6;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers; // JSON format for custom headers
    
    @Column(name = "authentication_required")
    private Boolean authenticationRequired = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}