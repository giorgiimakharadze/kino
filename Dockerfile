FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/kinodb
ENV APP_UPLOAD_DIR=/app/uploads
RUN mkdir -p /app/data /app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
