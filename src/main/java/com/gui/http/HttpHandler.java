package com.gui.http;

import java.io.IOException;

public interface HttpHandler {
    Response handle (Request request) throws IOException;
}
