package com.duncpro.restj.exceptional;

import com.duncpro.restj.HttpResponse;
import com.duncpro.restj.HttpStatus;

public class InternalServerErrorException extends RequestException {
    public InternalServerErrorException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.getCode());
    }
}
