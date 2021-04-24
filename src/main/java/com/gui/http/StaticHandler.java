package com.gui.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.gui.http.HttpStatus.*;

public class StaticHandler {

    private String rootPath;

    public StaticHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    public Response handle(Request request) throws IOException {

        if(methodNotImplemented(request)) {
            return response(NOT_IMPLEMENTED);
        }

        File f = new File(rootPath + request.getPath());
        if(!f.exists())
            return response(NOT_FOUND);

        if("HEAD".equals(request.getMethod())) {
            return response(OK);
        } else {
            byte[] body = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
            return response(OK, body);
        }
    }

    private Response response(HttpStatus status) {
        return new Response(status);
    }

    private Response response(HttpStatus status, byte[] body) {
        return new Response(status, body);
    }

    private boolean methodNotImplemented(Request request) {
        return !"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod());
    }
}
