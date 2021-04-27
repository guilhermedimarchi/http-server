package com.gui.http.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gui.http.util.HttpStatus.NOT_FOUND;
import static com.gui.http.util.HttpStatus.OK;
import static com.gui.http.util.StringUtil.LINE_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseTest {

    private ByteArrayOutputStream output;

    @BeforeEach
    public void setup() {
        output = new ByteArrayOutputStream();
    }

    @Test
    public void whenResponseIsSent_shouldWriteToOutputStream() throws IOException {
        new Response(NOT_FOUND).send(output);
        assertEquals("HTTP/1.1 404 Not Found", getResponseLines(output)[0]);
    }

    @Test
    public void whenResponseIsSentAndBodyIsGiven_shouldWriteBodyToOutputStream() throws IOException {
        byte[] body = "<html>cool page</html>".getBytes();

        new Response(OK, body).send(output);

        assertEquals("HTTP/1.1 200 Ok", getResponseLines(output)[0]);
        assertEquals("<html>cool page</html>", getResponseLines(output)[4]);
    }

    @Test
    public void whenResponseIsSentAndHeadersAreGiven_shouldWriteHeadersToOutputStream() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-length", "0");
        headers.put("Content-type", "text/html");

        new Response(OK, null, headers).send(output);

        assertEquals("HTTP/1.1 200 Ok", getResponseLines(output)[0]);
        assertEquals("Content-type: text/html", getHeaders(output).get(0));
        assertEquals("Content-length: 0", getHeaders(output).get(1));
    }

    private String[] getResponseLines(ByteArrayOutputStream out) {
        return out.toString().split(LINE_SEPARATOR);
    }

    private List<String> getHeaders(ByteArrayOutputStream out) {
        String[] lines = getResponseLines(out);
        List<String> headers = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            headers.add(lines[i]);
            if (lines[i].isBlank()) break;
        }
        return headers;
    }

}
