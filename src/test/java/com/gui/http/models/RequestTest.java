package com.gui.http.models;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals("request cannot be null or blank", ex.getMessage());
    }

    @Test
    public void whenFormatIsNotValid_shouldThrowRequestParseException() {
        RequestParseException ex = assertThrows(RequestParseException.class, () -> {
            createRequest("wrongformat");
        });
        assertEquals("missing http method or path", ex.getMessage());
    }

    private Request createRequest(String content) throws Exception {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }

}
