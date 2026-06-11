# Multi-stage build for UH Groupings API
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached layer)
RUN MAVEN_CONFIG="" ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for Docker build, run in CI/CD)
RUN MAVEN_CONFIG="" ./mvnw clean package -DskipTests -B

# Normalize executable artifact name for runtime stage copy
RUN cp /app/target/uhgroupingsapi-*.war /app/target/app.war

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-jammy

# Add labels for better container management
LABEL maintainer="University of Hawaii ITS"
LABEL application="uh-groupings-api"
LABEL description="UH Groupings API Service"

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy the built executable WAR from builder stage
COPY --from=builder /app/target/app.war app.war

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port 8081 to match application.properties
EXPOSE 8081

# Health check for ECS
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/uhgroupingsapi/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]