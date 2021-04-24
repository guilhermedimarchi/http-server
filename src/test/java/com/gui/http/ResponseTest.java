package com.gui.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.gui.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {

    private ByteArrayOutputStream output;

    @BeforeEach
    public void setup() {
        output = new ByteArrayOutputStream();
    }

    @Test
    public void whenResponseIsSent_shouldWriteToOutputStream() throws IOException {
        new Response(NOT_FOUND).send(output);
        assertEquals("HTTP/1.1 404 Not Found\r\n", output.toString());
    }

    @Test
    public void whenBodyIsGiven_shouldContainBodyToOutputStream() throws IOException {
        String body =  "<html>cool page</html>";
        new Response(OK, body.getBytes()).send(output);
        assertEquals("HTTP/1.1 200 Ok\r\n\r\n" + body, output.toString());
    }

    @Test
    public void whenHeadersAreGiven_shouldContainHeadersToOutputStream() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-length", "0");
        headers.put("Content-type", "text/html");

        new Response(OK, null, headers).send(output);
        assertEquals("HTTP/1.1 200 Ok\r\n" +
                "Content-type: text/html\r\n" +
                "Content-length: 0\r\n", output.toString());
    }

}
