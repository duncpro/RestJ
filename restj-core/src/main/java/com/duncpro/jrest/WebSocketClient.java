package com.duncpro.jrest;

import com.duncpro.jrest.integration.HttpIntegrator;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;

import javax.inject.Inject;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class WebSocketClient implements WebSocketRawClient {
    @Inject
    private WebSocketRawClient rawClient;

    @Inject
    private HttpIntegrator integrator;

    @Override
    public CompletableFuture<Void> sendMessage(byte[] message) {
        return rawClient.sendMessage(message);
    }

    public CompletableFuture<Void> sendMessage(Object deserializedForm) {
        final var rawData = integrator.serializeWebSocketMessage(deserializedForm);
        return rawClient.sendMessage(rawData);
    }

    @Override
    public CompletableFuture<Void> drop(int reasonCode, String reason) {
        return rawClient.drop(reasonCode, reason);
    }

    @Override
    public String getSessionId() {
        return rawClient.getSessionId();
    }

    @Override
    public HttpMethod getEndpointMethod() {
        return rawClient.getEndpointMethod();
    }

    @Override
    public Path getEndpointPath() {
        return rawClient.getEndpointPath();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WebSocketRawClient)) return false;
        return ((WebSocketRawClient) o).getSessionId().equals(this.getSessionId());
    }

    @Override
    public int hashCode() {
        return getSessionId().hashCode();
    }
}
