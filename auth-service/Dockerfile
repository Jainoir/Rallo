FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# The root pom declares all modules, so every module pom must exist
# for Maven to build the reactor — even when building just one module.
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY checkin-service/pom.xml checkin-service/
COPY notification-service/pom.xml notification-service/
RUN mvn dependency:go-offline -pl auth-service -am -q

COPY auth-service/src auth-service/src
RUN mvn package -pl auth-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
