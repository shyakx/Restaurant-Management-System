FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN ./gradlew build -x test
EXPOSE 8081
CMD ["java", "-jar", "build/libs/*.jar"]
