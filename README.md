
# http-server

Simple implementation of an HTTP webserver using Java

The server can serve files, allow navigating in directories and subdirectories inside root path.

If root path is not specified, by default, it will serve static content from folder www located in root of project.

Currently supports:
- GET request.
- HEAD request.
- Supports handling ETag, If-Match, If-None-Match, If-Modified-Since headers.
- Cache-Control (max-age has default of 0 so browser does not cache in disk and requests are sent to server to validate headers above).
- HTTP 1.1 Persistent connections (connection and keep alive headers, except timeout configuration. Default max requests per connection is set to 3).

Possible future extensions:
- Allow more handlers other than just static file handling.
- Allow configuring some parameters via env variables or main args.
- Timeout and close persistent sockets that were not used in last X milliseconds.
- Enable/disable directory exploring
- Non-blocking I/O
- JavaDoc

## Implementation approach and design considerations
The project was developed following Test Driven Development (bottom-up approach). Evolution of the codebase can be seen through commit history.
TDD allows for great confidence for code refactoring while making sure functionality is not affected. It strongly helps to reduce maintenance costs and increase speed to develop new functionalites, since its possible to quickly get feedback from the code that everything works as expected.

- App.java is where main method is located.


- HttpServer is responsible for accepting client connections and delegating it to ClientSocketManager in a new thread.


- ClientSocketManager is responsible for high level management of the request/response lifecycle, making sure request format is valid and handling possible exceptions that the handler itself did not catch. It also takes care of the persistent connection behavior by controlling some headers (connection and keep alive). Possible opportunity to remove the logic from persistent behavior from this class.  


- CachedFileHandler is a wrapper (proxy pattern) of the FileHandler . Basically it intercepts and control access to the more expensive object (StaticHandler). This is done via headers ETag, If-Match, If-None-Match, If-Modified-Since headers.


- FileHandler is responsible for reading the files or directories from disk.

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
