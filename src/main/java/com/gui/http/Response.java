package com.gui.http;

import java.io.IOException;
import java.io.OutputStream;

public class Response {

    private HttpStatus status;
    private byte[] body;

    public Response(HttpStatus status) {
        this.status = status;
    }

    public Response(HttpStatus status, byte[] body) {
        this(status);
        this.body = body;
    }

    public void send(OutputStream output) throws IOException {
        output.write(("HTTP/1.1 " + status.value() + " " + status.description()).getBytes());
        output.write("\r\n".getBytes());
        if(body != null) {
            output.write("\r\n".getBytes());
            output.write(this.body);
        }
        output.flush();
        output.close();
    }

}
