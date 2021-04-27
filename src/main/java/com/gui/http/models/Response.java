package com.gui.http.models;

import com.gui.http.util.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.gui.http.util.HttpHeader.CONTENT_LENGTH;
import static com.gui.http.util.HttpHeader.DATE;

public class Response {

    private final HttpStatus status;
    private byte[] body = new byte[0];
    private Map<String, String> headers = new HashMap<>();

    public Response(HttpStatus status) {
        this.status = status;
    }

    public Response(HttpStatus status, byte[] body) {
        this(status);
        this.body = body;
    }

    public Response(HttpStatus status, byte[] body, Map<String, String> headers) {
        this(status, body);
        this.headers = headers;
    }

    public void send(OutputStream output) throws IOException {
        PrintWriter printWriter = new PrintWriter(output, true, StandardCharsets.US_ASCII);
        writeStatusLine(printWriter);
        writeHeaders(printWriter);
        writeBody(output);
    }

    private void writeBody(OutputStream bos) throws IOException {
        if (body != null)
            bos.write(body, 0, body.length);
        bos.flush();
    }

    private void writeStatusLine(PrintWriter printer) {
        printer.println("HTTP/1.1 " + status.value() + " " + status.description());
    }

    private void writeHeaders(PrintWriter printer) {
        addHeader(DATE, new Date() + "");
        addHeader(CONTENT_LENGTH, body != null ? body.length + "" : "0");
        for (String header : headers.keySet()) {
            printer.println(header + ": " + headers.get(header));
        }
        printer.println();
        printer.flush();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return status == response.status && Arrays.equals(body, response.body) && Objects.equals(headers, response.headers);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(status, headers);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    public void addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

}
