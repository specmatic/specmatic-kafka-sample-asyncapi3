FROM eclipse-temurin:25.0.3_9-jre
WORKDIR /app
COPY build/libs/*.jar /app/app.jar
EXPOSE 8080 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
