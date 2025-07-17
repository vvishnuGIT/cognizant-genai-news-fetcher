package com.cognizant.genai.newsfetcher.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Slf4j
public class AzureConfig {
    
    @Value("${azure.client-id}")
    private String clientId;
    
    @Value("${azure.client-secret}")
    private String clientSecret;
    
    @Value("${azure.tenant-id}")
    private String tenantId;
    
    @Bean
    public ClientSecretCredential clientSecretCredential() {
        try {
            return new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to create Azure credentials: {}. SharePoint integration will be disabled.", e.getMessage());
            return null;
        }
    }
    
    @Bean
    public GraphServiceClient graphServiceClient(ClientSecretCredential credential) {
        if (credential == null) {
            log.warn("No Azure credentials available. SharePoint integration will be disabled.");
            return null;
        }
        
        try {
            TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                Arrays.asList("https://graph.microsoft.com/.default"), credential);
            
            return GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
        } catch (Exception e) {
            log.error("Failed to create Graph Service Client: {}", e.getMessage());
            return null;
        }
    }
}