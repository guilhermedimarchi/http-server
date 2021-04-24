package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;

import java.io.IOException;

public interface HttpHandler {
    Response handle (Request request) throws IOException;
}
