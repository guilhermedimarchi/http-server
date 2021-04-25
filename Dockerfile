FROM maven:3.6.0-jdk-11-slim AS build

WORKDIR /app
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn package
COPY ./www ./www

FROM openjdk:11-jre-slim

COPY --from=build app/target/http-server-*.jar ./http-server.jar
COPY --from=build app/www ./www
EXPOSE 8080
ENTRYPOINT ["java","-jar","http-server.jar"]