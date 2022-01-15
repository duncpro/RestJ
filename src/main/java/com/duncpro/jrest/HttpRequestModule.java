package com.duncpro.jrest;

import com.duncpro.jrest.integration.HttpIntegrator;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
