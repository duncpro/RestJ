package com.duncpro.restj.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow;

public class ConsolidatingByteArraySubscriber implements Flow.Subscriber<byte[]> {
    private final ConcurrentLinkedQueue<byte[]> buffers = new ConcurrentLinkedQueue<>();
    private final CompletableFuture<byte[]> future = new CompletableFuture<>();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        future.whenComplete((bytes, error) -> {
            if (error instanceof CancellationException) {
                subscription.cancel();
            }
        });
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(byte[] item) {
        buffers.add(item);
    }

    @Override
    public void onError(Throwable throwable) {
        future.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        final var all = buffers.stream()
                .reduce(ByteBufferUtils::concat)
                .orElse(new byte[0]);
        future.complete(all);
    }

    public CompletableFuture<byte[]> getFuture() {
        return future;
    }
}
