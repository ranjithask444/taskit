# Use a base image with JDK
FROM eclipse-temurin:21-jdk-alpine as builder

# Set working directory
WORKDIR /app

# Copy your project files (adjust for Maven or Gradle)
COPY . .

RUN chmod +x mvnw
# Build your project (Maven example)
RUN ./mvnw clean package -DskipTests

# ---- Production stage ----
FROM eclipse-temurin:21-jdk-alpine

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar /app/app.jar

# Expose port (Cloud Run default is 8080)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
