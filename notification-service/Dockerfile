FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY notification-service/pom.xml notification-service/
RUN mvn dependency:go-offline -pl notification-service -am -q

COPY notification-service/src notification-service/src
RUN mvn package -pl notification-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/notification-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
