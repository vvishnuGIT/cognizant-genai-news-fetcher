# 🚀 Quick Start Guide

This guide will help you get the Cognizant GenAI News Fetcher up and running quickly.

## 📋 Prerequisites

- **Java 17+** - [Download here](https://adoptium.net/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **Azure App Registration** - For SharePoint access (optional)

## 🎯 Option 1: Quick Local Run

```bash
# Clone the repository
git clone <repository-url>
cd cognizant-genai-news-fetcher

# Run the application (will use default test credentials)
./run.sh
```

The application will start on `http://localhost:8080`

## 🔧 Option 2: With SharePoint Integration

### Step 1: Azure App Registration

1. Go to [Azure Portal](https://portal.azure.com)
2. Navigate to **Azure Active Directory** → **App registrations** → **New registration**
3. Name: `GenAI News Fetcher`
4. Set redirect URI (optional for this app)
5. After creation, note down:
   - **Application (client) ID**
   - **Directory (tenant) ID**
6. Go to **Certificates & secrets** → **New client secret**
   - Note down the **secret value**
7. Go to **API permissions** → **Add a permission** → **Microsoft Graph**
   - Add **Application permissions**:
     - `Sites.Read.All`
     - `Files.Read.All`
   - Click **Grant admin consent**

### Step 2: Set Environment Variables

```bash
export AZURE_CLIENT_ID="your-client-id"
export AZURE_CLIENT_SECRET="your-client-secret"
export AZURE_TENANT_ID="your-tenant-id"
export SHAREPOINT_SITE_URL="https://cognizant.sharepoint.com/sites/your-site"
export SHAREPOINT_LIST_NAME="GenAI News"
```

### Step 3: Run the Application

```bash
./run.sh
```

## 🐳 Option 3: Docker

```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build Docker image manually
docker build -t genai-news-fetcher .
docker run -p 8080:8080 \
  -e AZURE_CLIENT_ID="your-client-id" \
  -e AZURE_CLIENT_SECRET="your-client-secret" \
  -e AZURE_TENANT_ID="your-tenant-id" \
  genai-news-fetcher
```

## 🌐 Accessing the Application

Once running, you can access:

- **📊 Web Dashboard**: http://localhost:8080
- **🗄️ H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:newsdb`
  - Username: `sa`
  - Password: (leave empty)
- **🔌 REST API**: http://localhost:8080/api
- **💓 Health Check**: http://localhost:8080/actuator/health

## 📱 Key Features

### Web Dashboard
- View all fetched news articles
- Search and filter articles
- Manual news fetch trigger
- Real-time statistics
- Source management

### API Endpoints
- `GET /api/news` - Get all articles
- `GET /api/news/search?query=AI` - Search articles
- `POST /api/news/fetch` - Trigger manual fetch
- `GET /api/news/statistics` - Get statistics
- `GET /api/sources` - Manage news sources

## 🎯 First Steps

1. **Access the Web Dashboard** at http://localhost:8080
2. **Click "Fetch News Now"** to get your first articles
3. **Explore the articles** using filters and search
4. **Check statistics** to see what's been fetched
5. **Configure additional sources** via the API

## 🛠️ Configuration

The application fetches from these external sources by default:
- TechCrunch AI
- MIT Technology Review AI  
- AI News
- OpenAI Blog
- Google AI Blog

You can add more sources via the REST API or by modifying `application.yml`.

## 🔍 SharePoint Setup

For SharePoint integration, ensure your SharePoint list has these columns:
- **Title** (Single line of text)
- **Description** (Multiple lines of text)
- **Content** (Multiple lines of text)
- **URL** (Hyperlink)
- **Author** (Single line of text)
- **PublishedDate** (Date and time)

## 🐛 Troubleshooting

### Application won't start
- Check Java version: `java -version` (should be 17+)
- Check Maven version: `mvn -version` (should be 3.6+)

### SharePoint errors
- Verify Azure app permissions are granted
- Check client ID, secret, and tenant ID
- Ensure SharePoint site URL is accessible

### No articles fetched
- Check internet connectivity
- Verify RSS feed URLs are accessible
- Check application logs for errors

## 📚 Next Steps

- Read the [full README](README.md) for detailed documentation
- Explore the API endpoints
- Set up automated deployment
- Configure additional news sources
- Customize the content analysis keywords

## 💡 Tips

- Use the H2 console to explore the database structure
- Check the `/actuator/health` endpoint for monitoring
- Use Docker for production deployments
- Set up environment-specific configurations

Need help? Check the logs or create an issue in the repository!