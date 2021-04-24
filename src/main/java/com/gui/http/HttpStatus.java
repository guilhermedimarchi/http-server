package com.gui.http;


enum HttpStatus {
    NOT_FOUND(404, "Not Found"),
    OK(200, "Ok"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int value;
    private final String description;

    HttpStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int value() {
        return this.value;
    }

    public String description() {
        return this.description;
    }
}
