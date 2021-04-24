package com.gui.http;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RequestTest {

    @Test
    public void whenFormatIsValid_shouldParseRequest() throws RequestParseException {
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

    private Request createRequest(String content) throws RequestParseException {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }

}
