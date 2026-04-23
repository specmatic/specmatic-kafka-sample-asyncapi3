FROM eclipse-temurin:25.0.2_10-jre
WORKDIR /app
COPY build/libs/*.jar /app/app.jar
EXPOSE 8080 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
