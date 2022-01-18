package com.duncpro.jrest;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import static java.util.Objects.requireNonNull;

class HttpEndpointModule extends AbstractModule {
    private final ParameterizableRoute route;

    HttpEndpointModule(ParameterizableRoute route) {
        this.route = requireNonNull(route);
    }

    @Override
    protected void configure() {
        bind(AsyncInjectionOrchestrator.class).in(Singleton.class);
        bind(ParameterizableRoute.class).toInstance(route);
    }
}
