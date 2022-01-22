package com.duncpro.restj;

import com.google.inject.AbstractModule;

import static java.util.Objects.requireNonNull;

public class SmokeTestModule extends AbstractModule {
    @Override
    public void configure() {
        final var endpointBinder = new HttpRestApiEndpointBinder(binder());
        endpointBinder.bind(SmokeTestApi.class);
    }
}
