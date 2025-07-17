package com.cognizant.genai.newsfetcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "news_articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NewsArticle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "source_name", nullable = false)
    private String sourceName;
    
    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;
    
    @Column(name = "published_date")
    private LocalDateTime publishedDate;
    
    @Column(name = "author")
    private String author;
    
    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
    
    @Column(name = "relevance_score")
    private Double relevanceScore;
    
    @Column(name = "sentiment_score")
    private Double sentimentScore;
    
    @Column(name = "is_from_sharepoint")
    private Boolean isFromSharepoint = false;
    
    @Column(name = "sharepoint_item_id")
    private String sharepointItemId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SourceType {
        RSS,
        SHAREPOINT,
        WEB_SCRAPING,
        API
    }
}