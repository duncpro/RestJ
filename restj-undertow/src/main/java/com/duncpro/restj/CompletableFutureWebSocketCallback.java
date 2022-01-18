package com.duncpro.restj;

import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

class CompletableFutureWebSocketCallback implements WebSocketCallback<Void> {
    private final CompletableFuture<Void> completion = new CompletableFuture<>();

    @Override
    public void complete(WebSocketChannel channel, Void context) {
        completion.complete(null);
    }

    @Override
    public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
        completion.completeExceptionally(throwable);
    }

    public CompletableFuture<Void> getCompletion() {
        return completion;
    }
}
