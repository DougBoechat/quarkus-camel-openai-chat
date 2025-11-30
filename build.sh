#!/bin/bash
echo "🚀 Building ZenFlow Backend..."
./mvnw clean package -DskipTests
echo "✅ Build completed!"