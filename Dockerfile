FROM gradle:8.14-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./

COPY gradlew ./
COPY gradle ./gradle

COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon


FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 19400

ENTRYPOINT ["java", "-jar", "/app/app.jar"]