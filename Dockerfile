FROM maven:3.6.0-jdk-11-slim AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src .
RUN mvn package

FROM openjdk:11-jre-slim
COPY --from=build /app/target .
EXPOSE 8080
ENTRYPOINT ["java","-jar","http-server-1.0-SNAPSHOT.jar"]