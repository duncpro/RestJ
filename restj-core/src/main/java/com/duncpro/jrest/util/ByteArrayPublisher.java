package com.duncpro.jrest.util;

import java.util.concurrent.Flow;

public class ByteArrayPublisher implements Flow.Publisher<byte[]> {
    private final byte[] byteBuffer;

    public ByteArrayPublisher(byte[] byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super byte[]> subscriber) {
        final var subscription = new Flow.Subscription() {
            @Override
            public void request(long n) {}

            @Override
            public void cancel() {}
        };
        subscriber.onSubscribe(subscription);
        subscriber.onNext(byteBuffer);
        subscriber.onComplete();
    }
}
