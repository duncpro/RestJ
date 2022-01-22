package com.duncpro.restj;

import com.google.inject.AbstractModule;

import static java.util.Objects.requireNonNull;

public class HttpRequestModule extends AbstractModule {
    private final HttpRequest request;

    public HttpRequestModule(HttpRequest request) {
        this.request = requireNonNull(request);
    }

    @Override
    public void configure() {
        bind(HttpRequest.class).toInstance(request);
    }
}
