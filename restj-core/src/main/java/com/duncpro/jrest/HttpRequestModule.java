package com.duncpro.jrest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import static java.util.Objects.requireNonNull;

public class HttpRequestModule extends AbstractModule {
    private final HttpRequest request;
    private final ParameterizableRoute route;

    public HttpRequestModule(HttpRequest request, ParameterizableRoute route) {
        this.request = requireNonNull(request);
        this.route = requireNonNull(route);
    }

    @Override
    public void configure() {
        bind(AsyncInjectionOrchestrator.class).in(Singleton.class);
        bind(ParameterizableRoute.class).toInstance(route);
        bind(HttpRequest.class).toInstance(request);
    }
}
