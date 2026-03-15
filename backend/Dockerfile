FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre AS final
WORKDIR /app

EXPOSE 8080
COPY --from=builder /app/build/libs/api-0.0.1.jar /app/api-0.0.1.jar

ENTRYPOINT ["java","-Duser.timezone=UTC","-jar","/app/api-0.0.1.jar","--spring.profiles.active=prod"]