package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;
import com.gui.http.util.HttpUtil;
import com.gui.http.util.StringUtil;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.gui.http.util.HttpHeader.*;
import static com.gui.http.util.HttpStatus.*;
import static com.gui.http.util.HttpUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle;


public class StaticHandlerTest {

    private static final String rootPath = new File("./src/test/resources/www").getAbsolutePath();
    private StaticHandler handler;

    @BeforeEach
    public void setup() {
        handler = new StaticHandler(rootPath);
    }

    private Request request(String content) throws Exception {
        return new Request(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class GivenPathIsFile {

        private byte[] body;
        private Map<String, String> headers;
        private final DateFormat formatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);

        @BeforeAll
        public void beforeAll() throws Exception {
            File f = new File(rootPath + "/index.html");
            body = Files.readAllBytes(f.toPath());
            BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            FileTime lastModified = attr.lastModifiedTime();
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            headers = Map.of(
                    CONTENT_TYPE, "text/html",
                    CONTENT_LENGTH, "" + body.length,
                    ETAG, StringUtil.toHex(f.getName() + attr.lastModifiedTime().toString() + attr.size()),
                    LAST_MODIFIED, formatter.format(new Date((lastModified.toMillis())))
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

    @Nested
    class GivenCachingBehavior {

        private ByteArrayOutputStream output;

        @BeforeEach
        public void setup() {
            output = new ByteArrayOutputStream();
        }

        @Test
        public void whenIfNoneMatchIsEqualEtag_shouldRespondNotModified() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            String etags = "etag1,etag2," + headerValueOf(ETAG, output.toString());
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_NONE_MATCH + ":" + etags));
            assertEquals(new Response(NOT_MODIFIED), actualResponse);
        }

        @Test
        public void whenIfNoneMatchIsStar_shouldRespondNotModified() throws Exception {
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_NONE_MATCH + ":*"));
            assertEquals(new Response(NOT_MODIFIED), actualResponse);
        }

        @Test
        public void whenIfMatchIsDifferentThenEtag_shouldRespondPreconditionFailed() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            String etag = "randometag";
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MATCH + ":" + etag));
            assertEquals(new Response(PRECONDITION_FAILED), actualResponse);
        }

        @Test
        public void whenIfMatchIsEqualToEtag_shouldProceedWithRequest() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            String etags = "etag1,etag2," + headerValueOf(ETAG, output.toString());
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MATCH + ":" + etags));
            assertNotEquals(new Response(PRECONDITION_FAILED), actualResponse);
        }

        @Test
        public void whenIfMatchIsStar_shouldProceedWithRequest() throws Exception {
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MATCH + ": *"));
            assertNotEquals(new Response(PRECONDITION_FAILED), actualResponse);
        }

        @Test
        public void whenIfModifiedSince_shouldRespondNotModified() throws Exception {
            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MODIFIED_SINCE + ":" + headerValueOf(LAST_MODIFIED, output.toString())));
            assertEquals(new Response(NOT_MODIFIED), actualResponse);
        }

        @Test
        public void whenResourceIsNewerThenClientCache_shouldProceedWithRequest() throws Exception {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(HTTP_DATE_FORMAT);

            handler.handle(request("GET /index.html HTTP/1.1")).send(output);
            ZonedDateTime date = ZonedDateTime.parse(headerValueOf(LAST_MODIFIED, output.toString()), dateFormatter).minusDays(1);

            Response actualResponse = handler.handle(request("GET /index.html HTTP/1.1\n" + IF_MODIFIED_SINCE + ":" + dateFormatter.format(date)));
            assertNotEquals(new Response(NOT_MODIFIED), actualResponse);
        }

        private String headerValueOf(String header, String response) {
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.contains(header))
                    return line.substring(line.indexOf(":") + 1).trim();
            }
            return "";
        }
    }
}
