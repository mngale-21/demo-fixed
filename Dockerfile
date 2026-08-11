# Hatua ya kwanza: Jenga mradi (Build)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Hatua ya pili: Run mradi
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/*.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]
