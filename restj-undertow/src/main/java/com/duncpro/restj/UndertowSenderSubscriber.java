package com.duncpro.restj;

import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.GracefulShutdownHandler;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.runAsync;

public class UndertowSenderSubscriber implements Flow.Subscriber<byte[]> {
    private Sender sender;

    private final CompletableFuture<?> completion = new CompletableFuture<>();

    UndertowSenderSubscriber(Sender sender) {
        this.sender = requireNonNull(sender);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(byte[] item) {
        sender.send(ByteBuffer.wrap(item));
    }

    @Override
    public void onError(Throwable throwable) {
        sender.close();
        completion.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        sender.close();
        completion.complete(null);
    }

    public CompletableFuture<?> getCompletion() {
        return completion;
    }
}
