# Stage 1: Build the bot
FROM gradle:8.7.0-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean shadowJar --no-daemon

# Stage 2: Run the bot
FROM eclipse-temurin:17-jre
WORKDIR /bot
COPY --from=builder /app/build/libs/*.jar main.jar

CMD ["java", "-jar", "Main.jar"]
