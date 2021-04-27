package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import static com.gui.http.util.HttpHeader.CONTENT_LENGTH;
import static com.gui.http.util.HttpHeader.CONTENT_TYPE;
import static com.gui.http.util.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle;


public class FileHandlerTest {

    private static final String rootPath = new File("./src/test/resources/www").getAbsolutePath();
    private FileHandler handler;

    @BeforeEach
    public void setup() {
        handler = new FileHandler(rootPath);
    }

    private Request request(String content) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(bis, StandardCharsets.US_ASCII));
        return new Request(in);
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class GivenPathToFile {

        private byte[] body;
        private Map<String, String> headers;

        @BeforeAll
        public void beforeAll() throws Exception {
            File f = new File(rootPath + "/index.html");
            body = Files.readAllBytes(f.toPath());
            headers = Map.of(
                    CONTENT_TYPE, "text/html",
                    CONTENT_LENGTH, "" + body.length
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
    class GivenPathToDirectory {
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
            assertTrue(response.contains("<pre><a href=\"" + File.separator + "folder1\">folder1</a>"), "expected to get HTML for root directory");
        }

        @Test
        public void whenAccessingInsideDirectory_shouldReturnExplorer() throws Exception {
            handler.handle(request("GET /folder1 HTTP/1.1")).send(out);
            String response = out.toString();
            assertTrue(response.contains("HTTP/1.1 200 Ok"), "expected to be 200 ok");
            assertTrue(response.contains("Content-Type: text/html"), "expected to have content type header");
            assertTrue(response.contains("Content-Length: "), "expected to have content length header");
            assertTrue(response.contains("Index of ." + File.separator + "folder1"), "expected to get HTML of ./folder1");
            assertTrue(response.contains("<a href=\"\">../</a>"), "expected to have link to go to parent folder");
        }

        @Test
        public void whenNestedDirectories_shouldReturnExplorer() throws Exception {
            handler.handle(request("GET /folder1/folder2 HTTP/1.1")).send(out);
            String response = out.toString();
            assertTrue(response.contains("HTTP/1.1 200 Ok"), "expected to be 200 ok");
            assertTrue(response.contains("Content-Type: text/html"), "expected to have content type header");
            assertTrue(response.contains("Content-Length: "), "expected to have content length header");
            assertTrue(response.contains("Index of ." + File.separator + "folder1" + File.separator + "folder2"), "expected to get HTML of ./folder1/folder2");
            assertTrue(response.contains("<a href=\"" + File.separator + "folder1\">../</a>"), "expected to have link to go to parent folder");
        }

        @Test
        public void whenDirectoryNameHasSpaces_shouldReturnExplorer() throws Exception {
            handler.handle(request("GET /folder1/folder2/folder%20with%20space HTTP/1.1")).send(out);
            String response = out.toString();
            assertTrue(response.contains("HTTP/1.1 200 Ok"), "expected to be 200 ok");
        }
    }

}
