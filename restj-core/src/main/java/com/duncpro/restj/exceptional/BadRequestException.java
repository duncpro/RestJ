package com.duncpro.restj.exceptional;

import com.duncpro.restj.HttpResponse;
import com.duncpro.restj.HttpStatus;

import java.util.List;
import java.util.Map;

public class BadRequestException extends RequestException {
    public BadRequestException(Exception cause) {
        super(cause);
    }

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpResponse getResponse() {
        if (getMessage() == null) {
            return new HttpResponse(HttpStatus.BAD_REQUEST.getCode());
        }

        return new HttpResponse(Map.of("Warning", List.of(getMessage())), null, HttpStatus.BAD_REQUEST.getCode());
    }
}
