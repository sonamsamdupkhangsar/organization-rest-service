FROM maven:3-openjdk-17-slim as build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN ["mvn", "clean", "install"]

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/organization-rest-service-1.0-SNAPSHOT.jar /app/organization-rest-service.jar
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/app/organization-rest-service.jar"]

