# ==============================================================================
# Multi-stage Dockerfile for CloudStorage Spring Boot Backend
# Optimized for Render Deployment & Container Security
# ==============================================================================

# Stage 1: Build JAR
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper & POM to leverage layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
# Pre-fetch dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and package application
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Run as non-privileged user for enhanced security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

# Render dynamically assigns PORT at runtime (default 8080 locally)
ENV PORT=8080
EXPOSE 8080

# Launch Spring Boot with container-aware memory flags & dynamic port binding
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]
