package com.duncpro.jrest.exceptional;

import com.duncpro.jrest.HttpResponse;
import com.duncpro.jrest.HttpStatus;
import com.duncpro.jrest.SerializedHttpResponse;

public class NotFoundException extends RequestException {
    public NotFoundException(Exception cause) {
        super(cause);
    }

    public NotFoundException() {
        super();
    }

    public SerializedHttpResponse getSerializedResponse() {
        return new SerializedHttpResponse(HttpStatus.NOT_FOUND.getCode());
    }

    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(HttpStatus.NOT_FOUND.getCode());
    }


}
