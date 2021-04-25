package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.gui.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class StaticHandlerTest {

    private static final String rootPath = new File("./src/test/resources/www").getAbsolutePath();
    private static byte[] body;
    private static Map<String, String> headers;
    private StaticHandler handler;

    @BeforeAll
    public static void beforeAll() throws Exception {
        body = Files.readAllBytes(Paths.get(rootPath + "/index.html"));
        headers = Map.of(
                "Content-Type", "text/html",
                "Content-Length", "" + body.length
        );
    }

    @BeforeEach
    public void setup() {
        handler = new StaticHandler(rootPath);
    }

    @Test
    public void whenRequestMethodNotSupported_shouldRespondNotImplemented() throws Exception {
        Response actualResponse = handler.handle(request("PUT / HTTP/1.1"));
        assertEquals(new Response(NOT_IMPLEMENTED), actualResponse);
    }

    @Test
    public void whenFileDoesNotExists_shouldRespondNotFound() throws Exception {
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
    public void whenPathIsDirectory_shouldReturnExplorer() throws Exception {
        Response actualResponse = handler.handle(request("GET / HTTP/1.1"));
        byte[] body = rootPageHtml().getBytes();
        Map<String, String> headers = getHeadersFor(body);
        assertEquals(new Response(OK, body, headers), actualResponse);
    }

    @Test
    public void whenPathIsDirectoryAndInsideDirectory_shouldReturnExplorer() throws Exception {
        Response actualResponse = handler.handle(request("GET /folder1 HTTP/1.1"));
        byte[] body = insideDirectoryHtml().getBytes();
        Map<String, String> headers = getHeadersFor(body);
        assertEquals(new Response(OK, body, headers), actualResponse);
    }

    @Test
    public void whenNestedDirectories_shouldReturnExplorer() throws Exception {
        Response actualResponse = handler.handle(request("GET /folder1/folder2 HTTP/1.1"));
        byte[] body = nestedDirectoriesHtml().getBytes();
        Map<String, String> headers = getHeadersFor(body);
        assertEquals(new Response(OK, body, headers), actualResponse);
    }

    @NotNull
    private Map<String, String> getHeadersFor(byte[] body) {
        return Map.of(
                "Content-Type", "text/html",
                "Content-Length", "" + body.length
        );
    }

    private String rootPageHtml() {
        return"<html><head><title>Index of </title></head><body><h1>Index of C:\\Users\\Guilherme\\Desktop\\http-server\\.\\src\\test\\resources\\www</h1><pre>Name | Last modified | Size</pre><hr/><pre><a href=\"C:\\Users\\Guilherme\\Desktop\\http-server\\.\\src\\test\\resources\">../</a>\n" +
                "<pre><a href=\"\\folder1\">folder1</a> | 2021-04-25T12:06:14.344388Z | 24 bytes</pre><pre><a href=\"\\index.html\">index.html</a> | 2021-04-25T09:02:41.643Z | 217 bytes</pre></body></html>";
    }

    private String insideDirectoryHtml() {
        return"<html><head><title>Index of \\folder1</title></head><body><h1>Index of C:\\Users\\Guilherme\\Desktop\\http-server\\.\\src\\test\\resources\\www\\folder1</h1><pre>Name | Last modified | Size</pre><hr/><pre><a href=\"\">../</a>\n" +
                "<pre><a href=\"\\folder1\\file1.txt\">file1.txt</a> | 2021-04-25T10:33:39.093Z | 15 bytes</pre><pre><a href=\"\\folder1\\folder2\">folder2</a> | 2021-04-25T12:06:14.340387Z | 9 bytes</pre></body></html>";
    }

    private String nestedDirectoriesHtml() {
        return"<html><head><title>Index of \\folder1\\folder2</title></head><body><h1>Index of C:\\Users\\Guilherme\\Desktop\\http-server\\.\\src\\test\\resources\\www\\folder1\\folder2</h1><pre>Name | Last modified | Size</pre><hr/><pre><a href=\"\\folder1\">../</a>\n" +
                "<pre><a href=\"\\folder1\\folder2\\file2.txt\">file2.txt</a> | 2021-04-25T10:33:19.442Z | 9 bytes</pre></body></html>";
    }

    private Request request(String content) throws Exception {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }
}
