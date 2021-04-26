package com.gui.http.handlers;

import com.gui.http.models.FileExplorerHtml;
import com.gui.http.models.Request;
import com.gui.http.models.Response;
import com.gui.http.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.gui.http.util.HttpHeader.*;
import static com.gui.http.util.HttpMethod.GET;
import static com.gui.http.util.HttpMethod.HEAD;
import static com.gui.http.util.HttpStatus.*;
import static com.gui.http.util.HttpUtil.HTTP_DATE_FORMAT;


public class StaticHandler implements HttpHandler {

    private final String rootPath;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(HTTP_DATE_FORMAT);

    public StaticHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public Response handle(Request request) throws IOException {
        if (methodNotImplemented(request))
            return new Response(NOT_IMPLEMENTED);

        String decodedPath = URLDecoder.decode(rootPath + request.getPath(), StandardCharsets.UTF_8);
        File file = new File(decodedPath);
        if (!file.exists())
            return new Response(NOT_FOUND);

        String etag = getEtag(file);
        if (ifMatch(request, etag))
            return new Response(PRECONDITION_FAILED);

        if (ifNoneMatch(request, etag))
            return new Response(NOT_MODIFIED);

        if (ifNotModified(request, file))
            return new Response(NOT_MODIFIED);

        byte[] body;
        if (file.isDirectory()) {
            FileExplorerHtml html = new FileExplorerHtml(file, rootPath);
            body = html.getHtmlBytes();
        } else {
            body = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        }

        Map<String, String> headers = getHeaders(file, etag, body);
        if (HEAD.equals(request.getMethod()))
            return new Response(OK, null, headers);
        else
            return new Response(OK, body, headers);
    }


    private Map<String, String> getHeaders(File file, String etag, byte[] body) throws IOException {
        return Map.of(
                CONTENT_TYPE, getContentType(file),
                CONTENT_LENGTH, "" + body.length,
                ETAG, etag,
                LAST_MODIFIED, getLastModified(file)
        );
    }

    private boolean ifNoneMatch(Request request, String etag) {
        if (!request.getHeaders().containsKey(IF_NONE_MATCH))
            return false;

        String etags = request.getHeaders().get(IF_NONE_MATCH);
        return (etags.contains("*") || etags.contains(etag));
    }

    private boolean ifMatch(Request request, String etag) {
        if (!request.getHeaders().containsKey(IF_MATCH))
            return false;

        String etags = request.getHeaders().get(IF_MATCH);
        return !(etags.contains("*") || etags.contains(etag));
    }

    private boolean ifNotModified(Request request, File file) throws IOException {
        if (!request.getHeaders().containsKey(IF_MODIFIED_SINCE))
            return false;
        ZonedDateTime cachedModified = ZonedDateTime.parse(request.getHeaders().get(IF_MODIFIED_SINCE), dateFormatter);
        ZonedDateTime lastModified = ZonedDateTime.parse(getLastModified(file), dateFormatter);
        return !lastModified.isAfter(cachedModified);
    }

    private String getContentType(File file) throws IOException {
        String type = Files.probeContentType(Paths.get(file.getAbsolutePath()));
        if (type == null)
            return "text/html";
        return type;
    }

    private String getLastModified(File file) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime lastModified = attr.lastModifiedTime();
        ZonedDateTime ldt = ZonedDateTime.ofInstant( lastModified.toInstant(), ZoneId.of("GMT"));
        return dateFormatter.format(ldt);
    }

    private String getEtag(File file) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return StringUtil.toHex(file.getName() + attr.lastModifiedTime().toString() + attr.size());
    }

    private boolean methodNotImplemented(Request request) {
        return !GET.equals(request.getMethod()) && !HEAD.equals(request.getMethod());
    }

}
