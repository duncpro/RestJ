package com.duncpro.restj.exceptional;

import com.duncpro.restj.HttpResponse;
import com.duncpro.restj.HttpStatus;
import com.duncpro.restj.SerializedHttpResponse;

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
