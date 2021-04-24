package com.gui.http;

public class RequestParseException extends Exception {
    public RequestParseException(String message) {
        super (message);
    }
    public RequestParseException(String message, Throwable cause) {
        super (message, cause);
    }
}