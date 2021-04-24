package com.gui.http;


enum HttpStatus {
    NOT_FOUND(404, "Not Found"),
    OK(200, "Ok"),
    NOT_IMPLEMENTED(501, "Not Implemented");

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
