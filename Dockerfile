FROM maven

WORKDIR /app

COPY pom.xml .
RUN mvn clean install

COPY . .

CMD ["java", "-cp", "./target/http-server-1.0-SNAPSHOT.jar", "com.gui.App"]