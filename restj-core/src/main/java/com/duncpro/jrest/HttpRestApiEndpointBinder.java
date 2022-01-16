package com.duncpro.jrest;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;

import javax.inject.Provider;
import java.util.Arrays;

public class HttpRestApiEndpointBinder {
    private final Provider<Injector> injector;
    private final Multibinder<DeclarativeHttpEndpoint> endpoints;

    public HttpRestApiEndpointBinder(Binder binder) {
        this.injector = binder.getProvider(Injector.class);
        this.endpoints = Multibinder.newSetBinder(binder, DeclarativeHttpEndpoint.class);
    }

    public void bind(Class<?> restResource) {
        Arrays.stream(restResource.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(HttpEndpoint.class))
                .map(method -> new DeclarativeHttpEndpoint(method, injector))
                .forEach(endpoint ->  endpoints.addBinding().toInstance(endpoint));
    }
}
