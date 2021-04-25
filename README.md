# http-server

## How to run

Build docker image:

`docker build -t guilhermedimarchi/webserver`

Run with default path:

`docker run -p 8080:8080 guilhermedimarchi/webserver`

Run with user specified root folder:

`docker run -p 8080:8080 -v C:/Users/Guilherme/Desktop:/www webserver` 
