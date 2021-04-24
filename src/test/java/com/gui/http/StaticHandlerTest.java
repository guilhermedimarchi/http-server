package com.gui.http;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.gui.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;


public class StaticHandlerTest {

    private StaticHandler handler;
    private static final String rootPath = new File("./src/test/resources").getAbsolutePath();
    private static byte[] body;
    private static Map<String, String> headers;

    @BeforeEach
    public void setup()  {
        handler = new StaticHandler(rootPath);
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        body = Files.readAllBytes(Paths.get(rootPath + "/index.html"));
        headers = Map.of(
                "Content-Type", "text/html",
                "Content-Length", "" + body.length
        );
    }

    @Test
    public void whenRequestMethodNotSupported_shouldRespondNotImplemented() throws Exception  {
        Response actualResponse = handler.handle(request("PUT / HTTP/1.1"));
        assertEquals(new Response(NOT_IMPLEMENTED), actualResponse);
    }

    @Test
    public void whenFileDoesNotExists_shouldRespondNotFound() throws Exception  {
        Response actualResponse = handler.handle(request("GET /non-existent-file.txt HTTP/1.1"));
        assertEquals(new Response(NOT_FOUND), actualResponse);
    }

    @Test
    public void whenFileExistsAndHeadMethod_shouldRespondOkWithoutBody() throws Exception {
        Response actualResponse = handler.handle(request("HEAD /index.html HTTP/1.1"));
        assertEquals(new Response(OK, null, headers), actualResponse);
    }

    @Test
    public void whenFileExistsAndGetMethod_shouldRespondOkWithBody() throws Exception {
        Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1"));
        assertEquals(new Response(OK, body, headers), actualResponse);
    }

    @Test
    public void whenPathIsDirectory_shouldRespondNotFound() throws Exception {
        Response actualResponse = handler.handle(request("GET / HTTP/1.1"));
        assertEquals(new Response(NOT_FOUND), actualResponse);
    }

    private Request request(String content) throws Exception {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }
}
