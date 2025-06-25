# -------- Stage 1: Build --------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Install Gradle 8.7 manually (or use your project's wrapper)
RUN apt-get update && apt-get install -y unzip wget && \
    wget https://services.gradle.org/distributions/gradle-8.7-bin.zip && \
    unzip gradle-8.7-bin.zip -d /opt && \
    ln -s /opt/gradle-8.7/bin/gradle /usr/bin/gradle

# Copy everything and build
COPY . .
RUN gradle shadowJar --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:24-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
