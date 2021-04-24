package com.gui.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Request {

    private String method;
    private String path;

    public Request(InputStream input) throws RequestParseException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String request = in.readLine();
            if(request == null || request.isBlank()) {
                throw new RequestParseException("inputStream cannot be null or blank");
            }
            String[] members = request.split(" ");
            method = members[0];
            path = members[1];
        } catch (IOException e) {
            throw new RequestParseException("could not parse request", e);
        }
    }
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}