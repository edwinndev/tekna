# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy project definition first for dependency layer caching
COPY pom.xml .

# Download dependencies (fail-safe cache warm-up)
RUN mvn dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create unprivileged system user and group for security
RUN addgroup -S tekna && adduser -S tekna -G tekna

# Copy packaged jar from builder stage
COPY --from=builder /build/target/tekna-api-*.jar /app/app.jar

# Set correct file ownership
RUN chown -R tekna:tekna /app

USER tekna:tekna

# Expose default HTTP port
EXPOSE 8080

# Configure JVM flags optimized for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
