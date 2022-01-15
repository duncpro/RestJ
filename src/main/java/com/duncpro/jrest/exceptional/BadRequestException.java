package com.duncpro.jrest.exceptional;

import com.duncpro.jrest.HttpResponse;
import com.duncpro.jrest.HttpStatus;

import static java.util.Objects.requireNonNull;

public class BadRequestException extends RequestException {
    public BadRequestException(Exception cause) {
        super(cause);
    }

    public BadRequestException() {
        super();
    }

    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(HttpStatus.BAD_REQUEST.getCode());
    }
}
