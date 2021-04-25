package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import com.gui.http.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static com.gui.http.HttpHeader.*;
import static com.gui.http.HttpStatus.*;


public class StaticHandler implements HttpHandler {

    private final String rootPath;

    public StaticHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public Response handle(Request request) throws IOException {
        if (methodNotImplemented(request))
            return new Response(NOT_IMPLEMENTED);

        File file = new File(rootPath + request.getPath());
        if (!file.exists())
            return new Response(NOT_FOUND);

        byte[] body;
        Map<String, String> headers = new HashMap<>();

        if (file.isDirectory()) {
            FileExplorerHtml html = new FileExplorerHtml(file, rootPath);
            body = html.getHtmlBytes();
            headers.put(CONTENT_TYPE, "text/html");
        } else {
            body = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            headers.put(CONTENT_TYPE, Files.probeContentType(Paths.get(file.getAbsolutePath())));
        }
        headers.put(CONTENT_LENGTH, "" + body.length);
        headers.put(ETAG, getEtag(file));

        if ("HEAD".equals(request.getMethod()))
            return new Response(OK, null, headers);
        else
            return new Response(OK, body, headers);
    }

    private String getEtag(File file) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return StringUtil.toHex(file.getName() + attr.lastModifiedTime().toString() + attr.size());
    }

    private boolean methodNotImplemented(Request request) {
        return !"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod());
    }

}
