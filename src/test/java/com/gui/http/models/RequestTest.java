package com.gui.http.models;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {

    @Test
    public void whenFormatIsValid_shouldParseRequest() throws Exception {
        Request r = createRequest("GET /image1.png HTTP/1.1");
        assertEquals("GET", r.getMethod());
        assertEquals("/image1.png", r.getPath());
    }

    @Test
    public void whenRequestIsBlank_shouldThrowRequestParseException() {
        RequestParseException ex = assertThrows(RequestParseException.class, () -> {
            createRequest("");
        });
        assertEquals("request line cannot be null or blank", ex.getMessage());
    }

    @Test
    public void whenFormatIsNotValid_shouldThrowRequestParseException() {
        RequestParseException ex = assertThrows(RequestParseException.class, () -> {
            createRequest("wrongformat");
        });
        assertEquals("missing http method or path", ex.getMessage());
    }

    @Test
    public void whenHeadersArePresent_shouldParseRequest() throws Exception {
        Request r = createRequest("GET /image1.png HTTP/1.1\nAccept-Language: en-us\nConnection:Keep-Alive\n\n");
        assertEquals("GET", r.getMethod());
        assertEquals("/image1.png", r.getPath());
        Map<String, String> actualHeaders = r.getHeaders();
        assertFalse(actualHeaders.isEmpty(), "headers should not be empty");
        assertEquals("en-us", actualHeaders.get("Accept-Language"));
        assertEquals("Keep-Alive", actualHeaders.get("Connection"));
    }

    @Test
    public void whenHeadersAreMalformed_shouldThrowRequestParseException() {
        RequestParseException ex = assertThrows(RequestParseException.class, () -> {
            createRequest("GET /image1.png HTTP/1.1\nAccept-Language");
        });
        assertEquals("request headers malformed", ex.getMessage());
    }

    private Request createRequest(String content) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(bis, StandardCharsets.US_ASCII));
        return new Request(in);
    }

}
