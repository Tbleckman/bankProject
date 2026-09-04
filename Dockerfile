FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre

WORKDIR /app

# curl is used by compose.yaml's healthcheck to poll /actuator/health;
# not present by default in the temurin jre base image
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/bankProject-1.0.0.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]