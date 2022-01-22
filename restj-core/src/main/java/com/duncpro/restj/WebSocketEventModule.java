package com.duncpro.restj;

import com.google.inject.AbstractModule;

import static java.util.Objects.requireNonNull;

class WebSocketEventModule extends AbstractModule {
    private final WebSocketRawClient wsClient;

    WebSocketEventModule(WebSocketRawClient client) {
        this.wsClient = requireNonNull(client);
    }

    @Override
    protected void configure() {
        bind(WebSocketRawClient.class).toInstance(wsClient);
    }
}
