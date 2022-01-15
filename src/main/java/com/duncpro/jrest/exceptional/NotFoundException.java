package com.duncpro.jrest.exceptional;

import com.duncpro.jrest.HttpResponse;
import com.duncpro.jrest.HttpStatus;

import static java.util.Objects.requireNonNull;

public class NotFoundException extends RequestException {
    public NotFoundException(Exception cause) {
        super(cause);
    }

    public NotFoundException() {
        super();
    }

    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(HttpStatus.NOT_FOUND.getCode());
    }
}
