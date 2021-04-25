package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.gui.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle;


public class StaticHandlerTest {

    private static final String rootPath = new File("./src/test/resources/www").getAbsolutePath();
    private StaticHandler handler;


    @BeforeEach
    public void setup() {
        handler = new StaticHandler(rootPath);
    }

    private Request request(String content) throws Exception {
        return new Request(new ByteArrayInputStream(content.getBytes()));
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class GivenPathIsFile {

        private byte[] body;
        private Map<String, String> headers;

        @BeforeAll
        public void beforeAll() throws Exception {
            body = Files.readAllBytes(Paths.get(rootPath + "/index.html"));
            headers = Map.of(
                    "Content-Type", "text/html",
                    "Content-Length", "" + body.length
            );
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
    }

    @Nested
    class GivenPathIsDirectory {
        private OutputStream out;

        @BeforeEach
        public void setup() {
            out = new ByteArrayOutputStream();
        }

        @Test
        public void whenRootDirectory_shouldReturnExplorer() throws Exception {
            handler.handle(request("GET / HTTP/1.1")).send(out);

            String response = out.toString();
            assertTrue(response.contains("HTTP/1.1 200 Ok"), "expected to be 200 ok");
            assertTrue(response.contains("Content-Type: text/html"), "expected to have content type header");
            assertTrue(response.contains("Content-Length: "), "expected to have content length header");
            assertTrue(response.contains("<h1>Index of .</h1>"), "expected to get HTML for root directory");
            assertTrue(response.contains("<pre><a href=\"\\folder1\">folder1</a>"), "expected to get HTML for root directory");
            assertTrue(response.contains("<pre><a href=\"\\index.html\">index.html</a>"), "expected to get HTML for root directory");
        }

        @Test
        public void whenAccessingInsideDirectory_shouldReturnExplorer() throws Exception {
            handler.handle(request("GET /folder1 HTTP/1.1")).send(out);
            String response = out.toString();
            assertTrue(response.contains("HTTP/1.1 200 Ok"), "expected to be 200 ok");
            assertTrue(response.contains("Content-Type: text/html"), "expected to have content type header");
            assertTrue(response.contains("Content-Length: "), "expected to have content length header");
            assertTrue(response.contains("<title>Index of .\\folder1</title>"), "expected to get HTML of ./folder1");
            assertTrue(response.contains("<a href=\"\">../</a>"), "expected to have link to go to parent folder");
        }

        @Test
        public void whenNestedDirectories_shouldReturnExplorer() throws Exception {
            handler.handle(request("GET /folder1/folder2 HTTP/1.1")).send(out);
            String response = out.toString();
            System.out.println(response);
            assertTrue(response.contains("HTTP/1.1 200 Ok"), "expected to be 200 ok");
            assertTrue(response.contains("Content-Type: text/html"), "expected to have content type header");
            assertTrue(response.contains("Content-Length: "), "expected to have content length header");
            assertTrue(response.contains("<title>Index of .\\folder1\\folder2</title>"), "expected to get HTML of ./folder1/folder2");
            assertTrue(response.contains("<a href=\"\\folder1\">../</a>"), "expected to have link to go to parent folder");
        }
    }
}
