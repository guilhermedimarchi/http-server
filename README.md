
# http-server

Simple implementation of a HTTP webserver using Java and following Test Driven Development (TDD). Evolution of the codebase can be seen through commit history.

The server can serve files, allow navigating in directories and subdirectories inside root path.

If root path is not specified, by default, it will serve static content from folder www located in root of project.

Currently supports:
- GET request
- HEAD request
- Supports handling ETag, If-Match, If-None-Match, If-Modified-Since headers
- Cache-Control max-age has default of 0 so that browser does not cache in disk and requests are sent to server to validate headers above

Future extensions:
- Persistent connections
- Configurable cache control max-age
- Allow more handlers other than just static file handling

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
