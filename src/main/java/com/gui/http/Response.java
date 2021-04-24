package com.gui.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class Response {

    private HttpStatus status;
    private byte[] body;
    private Map<String,String> headers;

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
        output.write(("HTTP/1.1 " + status.value() + " " + status.description() + "\r\n").getBytes());
        if(headers != null) {
            for(String header : headers.keySet()) {
                String line = header + ": " + headers.get(header);
                output.write(line.getBytes());
                output.write("\r\n".getBytes());
            }
        }
        if(body != null) {
            output.write("\r\n".getBytes());
            output.write(this.body);
        }
        output.flush();
        output.close();
    }

}