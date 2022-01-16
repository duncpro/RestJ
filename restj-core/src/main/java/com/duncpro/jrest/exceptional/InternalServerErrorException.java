package com.duncpro.jrest.exceptional;

import com.duncpro.jrest.HttpResponse;
import com.duncpro.jrest.HttpStatus;

public class InternalServerErrorException extends RequestException {
    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
    }
}
