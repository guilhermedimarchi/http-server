package com.gui.http.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final Map<String, String> headers = new HashMap<>();
    private String method;
    private String path;

    public Request(BufferedReader in) throws RequestParseException, IOException {
        try {
            String requestLine = in.readLine();
            setRequestLine(requestLine);
            String header = in.readLine();
            while (header != null && !header.isBlank()) {
                setHeaders(header);
                header = in.readLine();
            }

        } catch (IOException e) {
            throw new IOException("could not parse request", e);
        }
    }

    private void setRequestLine(String line) throws RequestParseException {
        if (line == null || line.isBlank())
            throw new RequestParseException("request line cannot be null or blank");

        String[] members = line.split(" ");
        if (members.length < 2)
            throw new RequestParseException("missing http method or path");
        method = members[0];
        path = members[1];
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    private void setHeaders(String header) throws RequestParseException {
        int idx = header.indexOf(":");
        if (idx == -1 || idx + 1 > header.length())
            throw new RequestParseException("request headers malformed");
        String key = header.substring(0, idx);
        String value = header.substring(idx + 1);
        headers.put(key.trim(), value.trim());
    }
}