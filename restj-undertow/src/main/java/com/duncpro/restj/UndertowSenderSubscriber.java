package com.duncpro.restj;

import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.GracefulShutdownHandler;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public class UndertowSenderSubscriber implements Flow.Subscriber<byte[]> {
    private Sender sender;
    private final HttpServerExchange exchange;
    private final Undertow server;

    UndertowSenderSubscriber(Undertow server, HttpServerExchange exchange) {
        this.server = requireNonNull(server);
        this.exchange = exchange;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.sender = exchange.getResponseSender();
    }

    @Override
    public void onNext(byte[] item) {
        sender.send(ByteBuffer.wrap(item));
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        sender.close();
        exchange.endExchange();

        if (throwable instanceof RuntimeException) {
            server.stop();
        }
    }

    @Override
    public void onComplete() {
        exchange.getResponseSender().close();
        exchange.endExchange();
    }
}
