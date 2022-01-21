package com.duncpro.restj;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

class CompletableFutureIOCallback implements IoCallback {
    private final CompletableFuture<Void> completion = new CompletableFuture<>();

    @Override
    public void onComplete(HttpServerExchange exchange, Sender sender) {
        completion.complete(null);
    }

    @Override
    public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
        completion.completeExceptionally(exception);
    }

    public CompletableFuture<Void> getCompletion() {
        return completion;
    }

}
