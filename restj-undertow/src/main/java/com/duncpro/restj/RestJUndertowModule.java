package com.duncpro.restj;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.undertow.Undertow;

import static java.util.Objects.requireNonNull;

public class RestJUndertowModule extends AbstractModule {
    private final Undertow.Builder builder;

    public RestJUndertowModule(Undertow.Builder builder) {
        this.builder = requireNonNull(builder);
    }

    @Override
    public void configure() {
        install(new HttpRestApiModule());
        bind(RestJUndertowHttpHandler.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    public Undertow providerServer(RestJUndertowHttpHandler handler) {
        return builder.setHandler(handler).build();
    }
}
