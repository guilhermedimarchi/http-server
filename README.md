
# http-server

Simple implementation of a HTTP webserver using Java. The server can serve static files and directories.  By default, it serves content from folder www located in root of project.

Currently supports only GET and HEAD requests.

## How to run

### Without docker
Requires maven installed.
Make sure you are in project root directory.

Run unit tests:

    mvn test

Creating artifact:

    mvn clean package

Then run artifact:

    java -jar target/http-server-1.0-SNAPSHOT.jar


### With docker

Build docker image locally:

    docker build -t http-server .


Run with default root path (will use default www folder with default content):


    docker run -p 8080:8080 http-server


Run with user specified root folder:

    docker run -p 8080:8080 -v CUSTOM_PATH:/www http-server

Example:

    docker run -p 8080:8080 -v C:/Users/Guilherme/Desktop:/www http-server
