package com.duncpro.jrest;

import static java.util.Objects.requireNonNull;

public enum HttpStatus {
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    NOT_ACCEPTABLE(406);

    private final int code;

    HttpStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
