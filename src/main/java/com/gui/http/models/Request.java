package com.gui.http.models;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private static final Logger LOGGER = Logger.getLogger(Request.class);
    private String method;
    private String path;
    private final Map<String, String> headers = new HashMap<>();

    public Request(InputStream input) throws RequestParseException, IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(input));

            String requestLine = in.readLine();
            LOGGER.debug("Request received: " + requestLine);
            setRequestLine(requestLine);

            String header = in.readLine();
            while (header != null) {
                setHeaders(header);
                header = in.readLine();
            }

        } catch (IOException e) {
            throw new IOException("could not parse request", e);
        }
    }

    private void setHeaders(String header) throws RequestParseException {
        String[] members = header.split(":");
        if (members.length < 2)
            throw new RequestParseException("request headers malformed");
        headers.put(members[0].trim(), members[1].trim());
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
}