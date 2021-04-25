
# http-server

Simple implementation of a HTTP webserver using Java. The server can serve static files and directories.  By default, it serves content from folder www located in root of project.

Currently supports only GET and HEAD requests.

## How to run

### Without docker
Requires maven installed.

Inside project folder, run:

    mvn clean package

Then run artifact:

    java -jar target/http-server-1.0-SNAPSHOT.jar

### With docker

Build docker image:


    docker build -t webserver


Run with default root path (will use default www folder with default content):


    docker run -p 8080:8080 webserver


Run with user specified root folder:

    docker run -p 8080:8080 -v CUSTOM_PATH:/www webserver

Example:

    docker run -p 8080:8080 -v C:/Users/Guilherme/Desktop:/www webserver
