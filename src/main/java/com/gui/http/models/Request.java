package com.gui.http.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Request {

    private final String method;
    private final String path;

    public Request(InputStream input) throws RequestParseException, IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));

            String request = in.readLine();
            if(request == null || request.isBlank())
                throw new RequestParseException("request cannot be null or blank");

            String[] members = request.split(" ");
            if(members.length < 2)
                throw new RequestParseException("missing http method or path");

            method = members[0];
            path = members[1];
        } catch (IOException e) {
            throw new IOException("could not parse request", e);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}