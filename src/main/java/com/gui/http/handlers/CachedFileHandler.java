package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import com.gui.http.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.gui.http.util.HttpHeader.*;
import static com.gui.http.util.HttpStatus.*;
import static com.gui.http.util.HttpUtil.HTTP_DATE_FORMAT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CachedFileHandler implements HttpHandler {

    private final FileHandler handler;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(HTTP_DATE_FORMAT);
    private int cacheControlMaxAge = 0;

    public CachedFileHandler(FileHandler handler) {
        this.handler = handler;
    }

    @Override
    public Response handle(Request request) throws IOException {
        String decodedPath = URLDecoder.decode(handler.getRootPath() + request.getPath(), UTF_8);
        File file = new File(decodedPath);
        if (!file.exists())
            return new Response(NOT_FOUND);

        String etag = getEtag(file);
        if (ifMatch(request, etag)) return new Response(PRECONDITION_FAILED);
        if (ifNoneMatch(request, etag)) return new Response(NOT_MODIFIED);
        if (ifNotModified(request, file)) return new Response(NOT_MODIFIED);

        Response response = this.handler.handle(request);

        response.addHeaders(getHeaders(file, etag));
        return response;
    }

    private Map<String, String> getHeaders(File file, String etag) throws IOException {
        return new HashMap<>(Map.of(
                ETAG, etag,
                LAST_MODIFIED, getLastModified(file),
                CACHE_CONTROL, "max-age=" + cacheControlMaxAge
        ));
    }

    private String getLastModified(File file) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime lastModified = attr.lastModifiedTime();
        ZonedDateTime ldt = ZonedDateTime.ofInstant(lastModified.toInstant(), ZoneId.of("GMT"));
        return dateFormatter.format(ldt);
    }

    private String getEtag(File file) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return StringUtil.toHex(file.getName() + attr.lastModifiedTime().toString() + attr.size());
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

    public void setCacheControlMaxAge(int cacheControlMaxAge) {
        this.cacheControlMaxAge = cacheControlMaxAge;
    }
}
