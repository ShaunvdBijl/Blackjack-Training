# ==============================================================================
# Stage 1: Build the Spring Boot application with Maven & JDK 17
# ==============================================================================
FROM maven:3.9-eclipse-temurin-26 AS builder

WORKDIR /workspace

# Cache Maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Runtime Image with GUI / X11 & Font Libraries for Java Swing
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install native X11, AWT, and font rendering libraries required by Java Swing
RUN apt-get update && apt-get install -y --no-install-recommends \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfreetype6 \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Copy built JAR and Basic Strategy reference chart image
COPY --from=builder /workspace/target/*.jar app.jar
COPY basicStrategy.png /app/basicStrategy.png

# Default X11 DISPLAY for Windows/macOS Docker Desktop (WSLg / VcXsrv / XQuartz)
ENV DISPLAY=host.docker.internal:0.0

ENTRYPOINT ["java", "-Djava.awt.headless=false", "-jar", "/app/app.jar"]
