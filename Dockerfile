# Build stage - Compile and package the application
FROM maven:3.9.2-openjdk-17-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# Runtime stage - Minimal image for deployment
FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder stage
COPY --from=builder /app/target/app.jar ./app.jar

# Create data directory for SQLite database
RUN mkdir -p /data

# Expose port 8080 (will be overridden by PORT env var on Render)
EXPOSE 8080

# Set environment variables
ENV PORT=8080
ENV DB_PATH=/data/data_access_logs.db

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT}/ || exit 1

# Run the application
# The PORT environment variable will be automatically set by Render
CMD ["java", "-jar", "app.jar"]
