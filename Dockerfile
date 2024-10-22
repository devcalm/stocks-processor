FROM gradle:8.8.0-jdk21 AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

RUN ./gradlew --no-daemon build -x test

COPY src src

RUN ./gradlew --no-daemon build -x test

FROM openjdk:21-jdk-slim AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]