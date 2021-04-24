package com.gui.http;

import java.io.File;

import static com.gui.http.HttpStatus.NOT_FOUND;
import static com.gui.http.HttpStatus.NOT_IMPLEMENTED;

public class StaticHandler {

    public Response handle(Request request) {

        if(methodNotImplemented(request)) {
            return new Response(NOT_IMPLEMENTED);
        }
        File f = new File(request.getPath());
        if(!f.exists())
            return new Response(NOT_FOUND);

        return null;
    }

    private boolean methodNotImplemented(Request request) {
        return !"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod());
    }
}
