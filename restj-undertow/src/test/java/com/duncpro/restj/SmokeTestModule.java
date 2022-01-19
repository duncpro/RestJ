package com.duncpro.restj;

import com.duncpro.jrest.HttpRestApiEndpointBinder;
import com.duncpro.jrest.integration.WebSocketMessageSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public class SmokeTestModule extends AbstractModule {
    @Override
    public void configure() {
        final var endpointBinder = new HttpRestApiEndpointBinder(binder());
        endpointBinder.bind(PingPongApi.class);
    }

    @Provides
    WebSocketMessageSerializer provideMessageSerializer() {
        return deserializedForm -> deserializedForm.toString().getBytes(StandardCharsets.UTF_8);
    }
}
