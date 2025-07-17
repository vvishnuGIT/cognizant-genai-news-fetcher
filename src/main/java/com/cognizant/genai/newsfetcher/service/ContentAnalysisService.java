package com.cognizant.genai.newsfetcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContentAnalysisService {
    
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    
    // Positive sentiment words related to AI/technology
    private static final Set<String> POSITIVE_WORDS = Set.of(
        "breakthrough", "innovative", "revolutionary", "advanced", "cutting-edge",
        "efficient", "powerful", "smart", "intelligent", "promising", "exciting",
        "improvement", "enhancement", "optimization", "success", "achievement",
        "solution", "benefit", "opportunity", "progress", "evolution"
    );
    
    // Negative sentiment words
    private static final Set<String> NEGATIVE_WORDS = Set.of(
        "risk", "danger", "threat", "concern", "problem", "issue", "challenge",
        "failure", "error", "bias", "limitation", "difficulty", "setback",
        "criticism", "controversy", "ethical", "regulation", "restriction"
    );
    
    public double calculateRelevanceScore(String content, Set<String> keywords) {
        if (content == null || content.trim().isEmpty() || keywords == null || keywords.isEmpty()) {
            return 0.0;
        }
        
        String lowercaseContent = content.toLowerCase();
        int keywordMatches = 0;
        int totalKeywords = keywords.size();
        
        // Count exact keyword matches
        for (String keyword : keywords) {
            if (lowercaseContent.contains(keyword.toLowerCase())) {
                keywordMatches++;
            }
        }
        
        // Base relevance score
        double relevanceScore = (double) keywordMatches / totalKeywords;
        
        // Boost score for multiple occurrences of the same keyword
        double boostFactor = 1.0;
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int occurrences = countOccurrences(lowercaseContent, lowerKeyword);
            if (occurrences > 1) {
                boostFactor += (occurrences - 1) * 0.1; // 10% boost per additional occurrence
            }
        }
        
        relevanceScore = Math.min(relevanceScore * boostFactor, 1.0);
        
        log.debug("Calculated relevance score: {} for content with {} keyword matches", 
                  relevanceScore, keywordMatches);
        
        return relevanceScore;
    }
    
    public double analyzeSentiment(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0; // Neutral
        }
        
        String lowercaseContent = content.toLowerCase();
        int positiveCount = 0;
        int negativeCount = 0;
        
        // Count positive sentiment words
        for (String positiveWord : POSITIVE_WORDS) {
            positiveCount += countOccurrences(lowercaseContent, positiveWord);
        }
        
        // Count negative sentiment words
        for (String negativeWord : NEGATIVE_WORDS) {
            negativeCount += countOccurrences(lowercaseContent, negativeWord);
        }
        
        // Calculate sentiment score between -1 (negative) and 1 (positive)
        int totalSentimentWords = positiveCount + negativeCount;
        if (totalSentimentWords == 0) {
            return 0.0; // Neutral
        }
        
        double sentimentScore = (double) (positiveCount - negativeCount) / totalSentimentWords;
        
        log.debug("Calculated sentiment score: {} (positive: {}, negative: {})", 
                  sentimentScore, positiveCount, negativeCount);
        
        return sentimentScore;
    }
    
    public Set<String> extractTags(String content, Set<String> keywords) {
        if (content == null || content.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> tags = new HashSet<>();
        String lowercaseContent = content.toLowerCase();
        
        // Add matched keywords as tags
        for (String keyword : keywords) {
            if (lowercaseContent.contains(keyword.toLowerCase())) {
                tags.add(keyword);
            }
        }
        
        // Add technology-specific tags based on content
        addTechnologyTags(lowercaseContent, tags);
        
        // Add sentiment-based tags
        double sentiment = analyzeSentiment(content);
        if (sentiment > 0.3) {
            tags.add("positive");
        } else if (sentiment < -0.3) {
            tags.add("negative");
        } else {
            tags.add("neutral");
        }
        
        log.debug("Extracted {} tags from content", tags.size());
        
        return tags;
    }
    
    private void addTechnologyTags(String content, Set<String> tags) {
        // AI/ML specific tags
        if (content.contains("neural network") || content.contains("deep learning")) {
            tags.add("deep-learning");
        }
        if (content.contains("transformer") || content.contains("attention")) {
            tags.add("transformer");
        }
        if (content.contains("llm") || content.contains("large language model")) {
            tags.add("large-language-model");
        }
        if (content.contains("gpt") || content.contains("chatgpt")) {
            tags.add("gpt");
        }
        if (content.contains("computer vision") || content.contains("image recognition")) {
            tags.add("computer-vision");
        }
        if (content.contains("nlp") || content.contains("natural language")) {
            tags.add("nlp");
        }
        if (content.contains("automation") || content.contains("automate")) {
            tags.add("automation");
        }
        if (content.contains("copilot") || content.contains("assistant")) {
            tags.add("ai-assistant");
        }
        
        // Business/Industry tags
        if (content.contains("enterprise") || content.contains("business")) {
            tags.add("enterprise");
        }
        if (content.contains("healthcare") || content.contains("medical")) {
            tags.add("healthcare");
        }
        if (content.contains("finance") || content.contains("fintech")) {
            tags.add("finance");
        }
        if (content.contains("education") || content.contains("learning")) {
            tags.add("education");
        }
        
        // Technology maturity tags
        if (content.contains("research") || content.contains("experimental")) {
            tags.add("research");
        }
        if (content.contains("production") || content.contains("commercial")) {
            tags.add("production");
        }
        if (content.contains("beta") || content.contains("preview")) {
            tags.add("beta");
        }
    }
    
    private int countOccurrences(String text, String substring) {
        if (text == null || substring == null || substring.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        
        return count;
    }
}