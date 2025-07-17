# Cognizant GenAI News Fetcher

A comprehensive Java Spring Boot application that automatically fetches GenAI-related news from both Cognizant SharePoint and external RSS sources. The application provides intelligent content analysis, sentiment scoring, and a modern web dashboard for managing and viewing news articles.

## 🚀 Features

- **Multi-Source Integration**: Fetches news from both Cognizant SharePoint and external RSS feeds
- **Intelligent Content Analysis**: AI-powered relevance scoring and sentiment analysis
- **Automated Scheduling**: Configurable automated news fetching every 6 hours
- **Modern Web Dashboard**: Beautiful, responsive UI for viewing and managing articles
- **Advanced Search**: Full-text search across all article content
- **Tag Extraction**: Automatic extraction of relevant tags from article content
- **Deduplication**: Smart deduplication to prevent duplicate articles
- **RESTful API**: Complete REST API for programmatic access
- **Statistics Dashboard**: Real-time statistics and analytics

## 🛠 Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security**
- **H2 Database** (in-memory for development)
- **Microsoft Graph API** (SharePoint integration)
- **Rome RSS Library** (RSS feed processing)
- **JSoup** (Web scraping)
- **Apache HttpClient 5**
- **Lombok**

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Azure App Registration (for SharePoint access)

## ⚙️ Configuration

### Azure/SharePoint Setup

1. Register an application in Azure Active Directory
2. Grant the following permissions:
   - `Sites.Read.All`
   - `Files.Read.All`
3. Configure the following environment variables or update `application.yml`:

```yaml
azure:
  client-id: your-azure-client-id
  client-secret: your-azure-client-secret
  tenant-id: your-azure-tenant-id
  sharepoint:
    site-url: https://cognizant.sharepoint.com/sites/your-site
    list-name: GenAI News
```

### Application Configuration

Key configuration options in `application.yml`:

```yaml
# News Sources
news:
  sources:
    external:
      - name: "TechCrunch AI"
        url: "https://techcrunch.com/category/artificial-intelligence/feed/"
        type: "RSS"
      # Add more sources as needed
  
  # Keywords for relevance scoring
  keywords:
    - "generative ai"
    - "genai"
    - "artificial intelligence"
    # Add more keywords as needed
  
  # Scheduling
  scheduling:
    fetch-interval: "0 0 */6 * * *" # Every 6 hours
    cleanup-interval: "0 0 2 * * *" # Daily cleanup at 2 AM
```

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd cognizant-genai-news-fetcher
   ```

2. **Configure Azure credentials** (create `application-local.yml` or set environment variables)
   ```yaml
   azure:
     client-id: your-client-id
     client-secret: your-client-secret
     tenant-id: your-tenant-id
   ```

3. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
   - Web Dashboard: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - API Documentation: http://localhost:8080/actuator

## 📊 Web Dashboard

The application includes a modern, responsive web dashboard accessible at `http://localhost:8080` with features:

- **Real-time Statistics**: View article counts, source breakdown, and recent activity
- **Manual Fetch Control**: Trigger immediate news fetching
- **Article Management**: View, search, and filter articles
- **Source Filtering**: View articles by source type (SharePoint vs External)
- **Advanced Search**: Full-text search across all article content
- **Responsive Design**: Mobile-friendly interface

## 🔗 API Endpoints

### News Articles

- `GET /api/news` - Get all articles (paginated)
- `GET /api/news/{id}` - Get specific article
- `GET /api/news/search?query={query}` - Search articles
- `GET /api/news/recent?days={days}` - Get recent articles
- `GET /api/news/sharepoint` - Get SharePoint articles only
- `GET /api/news/external` - Get external articles only
- `GET /api/news/relevant?minRelevanceScore={score}` - Get highly relevant articles
- `POST /api/news/fetch` - Trigger manual news fetch
- `GET /api/news/statistics` - Get application statistics

### News Sources

- `GET /api/sources` - Get all news sources
- `GET /api/sources/active` - Get active sources only
- `POST /api/sources` - Create new news source
- `PUT /api/sources/{id}` - Update news source
- `DELETE /api/sources/{id}` - Delete news source
- `POST /api/sources/{id}/activate` - Activate source
- `POST /api/sources/{id}/deactivate` - Deactivate source

## 🤖 Content Analysis

The application includes sophisticated content analysis features:

### Relevance Scoring
- Keyword-based relevance calculation
- Boost factors for multiple keyword occurrences
- Configurable minimum relevance thresholds

### Sentiment Analysis
- Technology-focused sentiment word lexicon
- Sentiment scoring from -1 (negative) to +1 (positive)
- Automatic sentiment-based tagging

### Tag Extraction
- Automatic extraction of technology-specific tags
- Industry and business context tags
- Technology maturity indicators

## 📅 Scheduling

The application automatically:
- Fetches news every 6 hours (configurable)
- Cleans up old articles daily (keeps 30 days by default)
- Updates source statistics in real-time

## 🗄️ Database Schema

### NewsArticle Entity
- Basic article information (title, description, content, URL)
- Source metadata (source name, type, SharePoint ID)
- Analysis results (relevance score, sentiment, tags)
- Timestamps (published date, created/updated)

### NewsSource Entity
- Source configuration (name, URL, type)
- Fetch metadata (last fetched, frequency, active status)
- Authentication settings

## 🔒 Security

- Spring Security integration for API protection
- Configurable CORS settings
- Secure Azure credential management
- Input validation and sanitization

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 📦 Deployment

### Production Build
```bash
mvn clean package -Pprod
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/genai-news-fetcher-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📈 Monitoring

The application includes Spring Boot Actuator endpoints for monitoring:
- `/actuator/health` - Application health check
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

## 🐛 Troubleshooting

### Common Issues

1. **SharePoint Authentication Errors**
   - Verify Azure app registration permissions
   - Check client ID, secret, and tenant ID
   - Ensure SharePoint site URL is correct

2. **RSS Feed Errors**
   - Check network connectivity
   - Verify RSS feed URLs are accessible
   - Review application logs for specific errors

3. **Database Issues**
   - H2 console available at `/h2-console` for debugging
   - Check application.yml database configuration

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋‍♂️ Support

For support and questions:
- Check the application logs for error details
- Review the API documentation
- Contact the development team