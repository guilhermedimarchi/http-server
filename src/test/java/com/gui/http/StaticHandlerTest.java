package com.gui.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.gui.http.HttpStatus.*;

public class StaticHandlerTest {

    private StaticHandler handler;
    private static final String rootPath = new File("./src/test/resources").getAbsolutePath();

    @BeforeEach
    public void setup() {
        handler = new StaticHandler(rootPath);
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

    @Test
    public void givenFileExistsAndHeadMethod_shouldRespondOkWithoutBody() throws Exception {
        Response actualResponse = handler.handle(request("HEAD /index.html HTTP/1.1"));
        Assertions.assertEquals(new Response(OK), actualResponse);
    }

    @Test
    public void givenFileExistsAndGetMethod_shouldRespondOkWithBody() throws Exception {
        Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1"));
        byte[] body = Files.readAllBytes(Paths.get(rootPath + "/index.html"));
        Assertions.assertEquals(new Response(OK, body), actualResponse);
    }

    private Request request(String content) throws RequestParseException {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }
}
