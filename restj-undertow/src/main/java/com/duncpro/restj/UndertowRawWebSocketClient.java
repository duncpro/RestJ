package com.duncpro.restj;

import com.duncpro.jrest.HttpEndpoint;
import com.duncpro.jrest.WebSocketRawClient;
import com.duncpro.jrest.util.FutureUtils;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;

import static java.util.Objects.compare;
import static java.util.Objects.requireNonNull;

class UndertowRawWebSocketClient implements WebSocketRawClient {
    private final WebSocketChannel undertowChannel;
    private final HttpMethod endpointMethod;
    private final Path endpointPath;

    UndertowRawWebSocketClient(HttpMethod endpointMethod, Path endpointPath, WebSocketChannel undertowChannel) {
        this.undertowChannel = requireNonNull(undertowChannel);
        this.endpointMethod = requireNonNull(endpointMethod);
        this.endpointPath = requireNonNull(endpointPath);
    }

    @Override
    public CompletableFuture<Void> sendMessage(byte[] message) {
        final var sent = new CompletableFutureWebSocketCallback();
        WebSockets.sendBinary(ByteBuffer.wrap(message), undertowChannel, sent);
        return sent.getCompletion();
    }

    @Override
    public CompletableFuture<Void> drop(int reasonCode, String reason) {
        final var sent = new CompletableFutureWebSocketCallback();
        WebSockets.sendClose(reasonCode, reason, undertowChannel, sent);
        return sent.getCompletion().handle(($, error) -> {
            try {
                undertowChannel.close();
            } catch (IOException e) {
                if (error == null) throw new CompletionException(e);
                error.addSuppressed(e);
            }
            if (error != null) throw FutureUtils.wrapException(error);
            return null;
        });
    }


    @Override
    public String getSessionId() {
        return ((UUID) undertowChannel.getAttribute("sessionId")).toString();
    }

    @Override
    public HttpMethod getEndpointMethod() {
        return endpointMethod;
    }

    @Override
    public Path getEndpointPath() {
        return endpointPath;
    }
}
