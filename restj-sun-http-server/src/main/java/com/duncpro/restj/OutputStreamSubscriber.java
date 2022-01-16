package com.duncpro.restj;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public class OutputStreamSubscriber implements Flow.Subscriber<byte[]> {
    private final OutputStream outputStream;
    private final CompletableFuture<Void> completion;

    public OutputStreamSubscriber(OutputStream outputStream) {
        this.outputStream = requireNonNull(outputStream);
        this.completion = new CompletableFuture<>();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {}

    @Override
    public void onNext(byte[] item) {
        try {
            outputStream.write(item);
        } catch (IOException e) {
            completion.completeExceptionally(e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        try {
            outputStream.close();
        } catch (IOException e) {
            completion.completeExceptionally(e);
        }
    }

    @Override
    public void onComplete() {
        try {
            outputStream.close();
        } catch (IOException e) {
            completion.completeExceptionally(e);
            return;
        }
        completion.complete(null);
    }

    public CompletableFuture<Void> getCompletion() {
        return completion;
    }
}
