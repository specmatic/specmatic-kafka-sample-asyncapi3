FROM eclipse-temurin:25.0.2_10-jre
WORKDIR /app
COPY build/libs/*.jar /app/app.jar
EXPOSE 9000 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
