FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY checkin-service/pom.xml checkin-service/
RUN mvn dependency:go-offline -pl checkin-service -am -q

COPY checkin-service/src checkin-service/src
RUN mvn package -pl checkin-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/checkin-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
