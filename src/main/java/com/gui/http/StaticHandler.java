package com.gui.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.gui.http.HttpStatus.*;

public class StaticHandler implements HttpHandler {

    private final String rootPath;

    public StaticHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public Response handle(Request request) throws IOException {
        if(methodNotImplemented(request))
            return new Response(NOT_IMPLEMENTED);

        File file = new File(rootPath + request.getPath());
        if(!file.exists() || file.isDirectory())
            return new Response(NOT_FOUND);

        byte[] body = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        Map<String, String> headers = Map.of(
                "Content-Type", Files.probeContentType(Paths.get(file.getAbsolutePath())),
                "Content-Length", "" + body.length
        );

        if("HEAD".equals(request.getMethod()))
            return new Response(OK, null, headers);
        else
            return new Response(OK, body, headers);
    }

    private boolean methodNotImplemented(Request request) {
        return !"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod());
    }
}
