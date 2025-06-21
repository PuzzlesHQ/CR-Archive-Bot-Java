# build stage
FROM maven:3.9-eclipse-temurin-24 AS build
COPY pom.xml src/ ./
RUN mvn clean package -DskipTests

# runtime stage
FROM eclipse-temurin:24-jdk
COPY --from=build target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
