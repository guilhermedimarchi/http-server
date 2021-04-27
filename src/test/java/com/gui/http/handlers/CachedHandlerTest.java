package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.gui.http.util.HttpHeader.*;
import static com.gui.http.util.HttpStatus.*;
import static com.gui.http.util.HttpUtil.HTTP_DATE_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CachedHandlerTest {

    private static final String rootPath = new File("./src/test/resources/www").getAbsolutePath();
    private ByteArrayOutputStream output;
    private CachedHandler handler;

    @BeforeEach
    public void setup() {
        handler = new CachedHandler(new StaticHandler(rootPath));
        output = new ByteArrayOutputStream();
    }

    private Request request(String content) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(bis, StandardCharsets.US_ASCII));
        return new Request(in);
    }

    private String headerValueOf(String header, String response) {
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.contains(header))
                return line.substring(line.indexOf(":") + 1).trim();
        }
        return "";
    }

    @Test
    public void whenFileDoesNotExists_shouldRespondNotFound() throws Exception {
        Response actualResponse = handler.handle(request("GET /non-existent-file.txt HTTP/1.1"));
        assertEquals(new Response(NOT_FOUND), actualResponse);
    }

    @Nested
    class GivenIfNoneMatchHeader {
        @Test
        public void whenIsEqualEtag_shouldRespondNotModified() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            String etags = "etag1,etag2," + headerValueOf(ETAG, output.toString());
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_NONE_MATCH + ":" + etags));
            assertEquals(new Response(NOT_MODIFIED), actualResponse);
        }

        @Test
        public void whenIsStar_shouldRespondNotModified() throws Exception {
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_NONE_MATCH + ":*"));
            assertEquals(new Response(NOT_MODIFIED), actualResponse);
        }
    }

    @Nested
    class GivenIfMatchHeader {
        @Test
        public void whenIsDifferentThenEtag_shouldRespondPreconditionFailed() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            String etag = "randometag";
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MATCH + ":" + etag));
            assertEquals(new Response(PRECONDITION_FAILED), actualResponse);
        }

        @Test
        public void whenIsEqualToEtag_shouldProceedWithRequest() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            String etags = "etag1,etag2," + headerValueOf(ETAG, output.toString());
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MATCH + ":" + etags));
            assertNotEquals(new Response(PRECONDITION_FAILED), actualResponse);
        }

        @Test
        public void whenIsStar_shouldProceedWithRequest() throws Exception {
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MATCH + ": *"));
            assertNotEquals(new Response(PRECONDITION_FAILED), actualResponse);
        }
    }

    @Nested
    class GivenIfModifiedSinceHeader {

        @Test
        public void whenResourceIsSameDate_shouldRespondNotModified() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MODIFIED_SINCE + ":" + headerValueOf(LAST_MODIFIED, output.toString())));
            assertEquals(new Response(NOT_MODIFIED), actualResponse);
        }

        @Test
        public void whenResourceIsNewer_shouldProceedWithRequest() throws Exception {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(HTTP_DATE_FORMAT);

            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            ZonedDateTime date = ZonedDateTime.parse(headerValueOf(LAST_MODIFIED, output.toString()), dateFormatter).minusDays(1);

            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MODIFIED_SINCE + ":" + dateFormatter.format(date)));
            assertNotEquals(new Response(NOT_MODIFIED), actualResponse);
        }
    }
}
