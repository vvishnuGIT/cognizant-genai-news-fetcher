#!/bin/bash

# Cognizant GenAI News Fetcher - Run Script

echo "🤖 Starting Cognizant GenAI News Fetcher..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Set default values for Azure credentials if not provided
export AZURE_CLIENT_ID=${AZURE_CLIENT_ID:-"your-client-id"}
export AZURE_CLIENT_SECRET=${AZURE_CLIENT_SECRET:-"your-client-secret"}
export AZURE_TENANT_ID=${AZURE_TENANT_ID:-"your-tenant-id"}
export SHAREPOINT_SITE_URL=${SHAREPOINT_SITE_URL:-"https://cognizant.sharepoint.com/sites/your-site"}
export SHAREPOINT_LIST_NAME=${SHAREPOINT_LIST_NAME:-"GenAI News"}

echo "📋 Configuration:"
echo "   • Azure Client ID: ${AZURE_CLIENT_ID}"
echo "   • SharePoint Site: ${SHAREPOINT_SITE_URL}"
echo "   • SharePoint List: ${SHAREPOINT_LIST_NAME}"

# Build the application
echo "🔨 Building application..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo "✅ Build successful!"

# Run the application
echo "🚀 Starting the application..."
echo "   • Web Dashboard: http://localhost:8080"
echo "   • H2 Console: http://localhost:8080/h2-console"
echo "   • API Base: http://localhost:8080/api"
echo ""
echo "Press Ctrl+C to stop the application"
echo "================================================"

mvn spring-boot:run