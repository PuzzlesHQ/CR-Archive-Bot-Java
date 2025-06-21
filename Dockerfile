# -------- Stage 1: Build --------
FROM gradle:8.7-eclipse-temurin-24 AS build
WORKDIR /app

# Only copy build files first for layer caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle build --no-daemon || true  # warm up Gradle, ignore failure

# Copy the rest and build
COPY . .
RUN gradle build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:24-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
