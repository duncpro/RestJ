package com.duncpro.restj.exceptional;

import com.duncpro.restj.HttpResponse;
import com.duncpro.restj.HttpStatus;
import com.duncpro.restj.SerializedHttpResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentNegotiationException extends RequestException {
    public ContentNegotiationException(Set<String> clientSupportedContentTypes, Set<String> serverSupportedContentTypes) {
        super("This server does not support any of the Content-Types declared within the request's Accept header." +
                " The server supports: " + serverSupportedContentTypes + " but the client supports: " + clientSupportedContentTypes);
    }

    @Override
    public HttpResponse getResponse() {
        final Map<String, List<Object>> header = Map.of("Warning", List.of(this.getMessage()));
        return new HttpResponse(header, null, HttpStatus.NOT_ACCEPTABLE.getCode());
    }

    public SerializedHttpResponse getSerializedResponse() {
        final Map<String, List<String>> header = Map.of("Warning", List.of(this.getMessage()));
        return new SerializedHttpResponse(header, null, HttpStatus.NOT_ACCEPTABLE.getCode());
    }
}
