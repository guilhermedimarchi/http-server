package com.gui.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {

    @Test
    public void whenFormatIsValid_shouldParseRequest() throws RequestParseException {
        InputStream input = new ByteArrayInputStream("GET /image1.png HTTP/1.1".getBytes());
        Request r = new Request(input);
        assertEquals("GET", r.getMethod());
        assertEquals("/image1.png", r.getPath());
    }

    @Test
    public void whenFormatIsNotValid_shouldThrowRequestParseExceptionException() {
        InputStream input = new ByteArrayInputStream("".getBytes());
        RequestParseException ex = assertThrows(RequestParseException.class, () -> {
            Request r = new Request(input);
        });
        assertEquals("inputStream cannot be null or blank", ex.getMessage());
    }

}
