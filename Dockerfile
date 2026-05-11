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

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=builder /app/build/libs/app.jar /app/app.jar

RUN chown app:app /app/app.jar

USER app

EXPOSE 19400

ENTRYPOINT ["java", "-jar", "app.jar"]