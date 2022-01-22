package com.duncpro.restj;

import io.undertow.io.Sender;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.runAsync;

public class PipeToClient implements Flow.Subscriber<byte[]> {
    private final Sender sender;

    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private volatile Flow.Subscription subscription;

    PipeToClient(Sender sender) {
        this.sender = requireNonNull(sender);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(byte[] item) {
        final var callback = new CompletableFutureIOCallback();
        sender.send(ByteBuffer.wrap(item), callback);
        callback.getCompletion().whenComplete((data, error) -> {
            if (error != null) {
                // Delivery error unrelated to application flow
                subscription.cancel();
                error.printStackTrace();
                completion.complete(null);
                return;
            }
            subscription.request(1);
        });
    }

    @Override
    public void onError(Throwable throwable) {
        completion.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        completion.complete(null);
    }

    public CompletableFuture<Void> getCompletion() {
        return completion;
    }
}
