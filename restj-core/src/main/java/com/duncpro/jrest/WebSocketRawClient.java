package com.duncpro.jrest;

import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public interface WebSocketRawClient {
    CompletableFuture<Void> sendMessage(byte[] message);

    CompletableFuture<Void> drop(int reasonCode, String reason);

    String getSessionId();

    HttpMethod getEndpointMethod();

    Path getEndpointPath();
}
