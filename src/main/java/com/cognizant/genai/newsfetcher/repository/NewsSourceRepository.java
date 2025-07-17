package com.cognizant.genai.newsfetcher.repository;

import com.cognizant.genai.newsfetcher.model.NewsArticle;
import com.cognizant.genai.newsfetcher.model.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {
    
    Optional<NewsSource> findByName(String name);
    
    List<NewsSource> findByIsActive(Boolean isActive);
    
    List<NewsSource> findBySourceType(NewsArticle.SourceType sourceType);
    
    List<NewsSource> findByIsActiveAndSourceType(Boolean isActive, NewsArticle.SourceType sourceType);
    
    @Query("SELECT s FROM NewsSource s WHERE s.isActive = true AND " +
           "(s.lastFetched IS NULL OR s.lastFetched <= :cutoffTime)")
    List<NewsSource> findSourcesDueForFetch(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    boolean existsByName(String name);
    
    boolean existsByUrl(String url);
}