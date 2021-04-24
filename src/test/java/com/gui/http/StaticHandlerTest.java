package com.gui.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static com.gui.http.HttpStatus.NOT_FOUND;
import static com.gui.http.HttpStatus.NOT_IMPLEMENTED;


public class StaticHandlerTest {

    private StaticHandler handler;

    @BeforeEach
    public void setup() {
        handler = new StaticHandler();
    }

    @Test
    public void givenRequestMethodNotSupported_shouldRespondNotImplemented() throws Exception  {
        Response actualResponse = handler.handle(request("PUT / HTTP/1.1"));
        Assertions.assertEquals(new Response(NOT_IMPLEMENTED), actualResponse);
    }

    @Test
    public void givenFileDoesNotExists_shouldRespondNotFound() throws Exception  {
        Response actualResponse = handler.handle(request("GET /non-existent-file.txt HTTP/1.1"));
        Assertions.assertEquals(new Response(NOT_FOUND), actualResponse);
    }

    private Request request(String content) throws RequestParseException {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }
}
