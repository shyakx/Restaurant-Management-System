FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN ./gradlew build -x test
EXPOSE 8761
CMD ["java", "-jar", "build/libs/*.jar"]
