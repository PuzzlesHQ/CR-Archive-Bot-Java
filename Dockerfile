# ---------- Stage 1: Build ----------
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app

# Copy everything and build the fat jar
COPY . .
RUN gradle shadowJar --no-daemon

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy fat JAR from builder
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Expose port (update if your app uses a different port)
EXPOSE 8080

# Run it
ENTRYPOINT ["java", "-jar", "app.jar"]
