package com.gui.http.handlers;

import com.gui.http.models.FileExplorerHtml;
import com.gui.http.models.Request;
import com.gui.http.models.Response;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.gui.http.util.HttpHeader.CONTENT_LENGTH;
import static com.gui.http.util.HttpHeader.CONTENT_TYPE;
import static com.gui.http.util.HttpMethod.GET;
import static com.gui.http.util.HttpMethod.HEAD;
import static com.gui.http.util.HttpStatus.*;
import static java.nio.charset.StandardCharsets.UTF_8;


public class FileHandler implements HttpHandler {

    private final String rootPath;

    public FileHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    public Response handle(Request request) throws IOException {
        if (methodNotImplemented(request))
            return new Response(NOT_IMPLEMENTED);

        String decodedPath = URLDecoder.decode(rootPath + request.getPath(), UTF_8);
        File file = new File(decodedPath);
        if (!file.exists())
            return new Response(NOT_FOUND);

        byte[] body;
        if (file.isDirectory()) {
            FileExplorerHtml html = new FileExplorerHtml(file, rootPath);
            body = html.getHtmlBytes();
        } else {
            body = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        }

        Map<String, String> headers = getHeadersFor(file, body);
        if (HEAD.equals(request.getMethod()))
            return new Response(OK, null, headers);
        else
            return new Response(OK, body, headers);
    }

    private Map<String, String> getHeadersFor(File file, byte[] body) throws IOException {
        return new HashMap<>(Map.of(
                CONTENT_TYPE, getContentType(file),
                CONTENT_LENGTH, "" + body.length
        ));
    }

    private String getContentType(File file) throws IOException {
        String type = Files.probeContentType(Paths.get(file.getAbsolutePath()));
        if (type == null)
            return "text/html";
        return type;
    }


    private boolean methodNotImplemented(Request request) {
        return !GET.equals(request.getMethod()) && !HEAD.equals(request.getMethod());
    }


    public String getRootPath() {
        return rootPath;
    }
}
