package com.gui.http;

import java.io.IOException;
import java.io.OutputStream;

public class Response {

    private HttpStatus status;

    public Response(HttpStatus status) {
        this.status = status;
    }

    public void send(OutputStream output) throws IOException {
        output.write(("HTTP/1.1 " + status.value() + " " + status.description()).getBytes());
        output.write("\r\n".getBytes());
        output.flush();
        output.close();
    }

}
