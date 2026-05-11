FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon || true


COPY src src


RUN ./gradlew bootJar --no-daemon -x test -x asciidoctor

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

EXPOSE 19400

ENTRYPOINT ["java", "-jar", "/app/app.jar"]