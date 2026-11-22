FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY clinica-service/pom.xml clinica-service/
COPY clinica-service/src clinica-service/src
RUN mvn -q -pl clinica-service -am package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/clinica-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
