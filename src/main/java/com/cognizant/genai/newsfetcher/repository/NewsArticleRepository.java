package com.cognizant.genai.newsfetcher.repository;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    
    Optional<NewsArticle> findByUrl(String url);
    
    boolean existsByUrl(String url);
    
    List<NewsArticle> findBySourceName(String sourceName);
    
    List<NewsArticle> findBySourceType(NewsArticle.SourceType sourceType);
    
    List<NewsArticle> findByIsFromSharepoint(Boolean isFromSharepoint);
    
    Optional<NewsArticle> findBySharepointItemId(String sharepointItemId);
    
    Page<NewsArticle> findByPublishedDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<NewsArticle> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<NewsArticle> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "a.relevanceScore >= :minScore")
    Page<NewsArticle> findByRelevanceScoreGreaterThanEqual(@Param("minScore") Double minScore, Pageable pageable);
    
    @Query("SELECT a FROM NewsArticle a WHERE " +
           "a.publishedDate >= :since ORDER BY a.publishedDate DESC")
    List<NewsArticle> findRecentArticles(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM NewsArticle a WHERE a.createdAt >= :since")
    Long countArticlesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.sourceName, COUNT(a) FROM NewsArticle a GROUP BY a.sourceName")
    List<Object[]> getArticleCountBySource();
    
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}