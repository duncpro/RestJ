package com.duncpro.restj;

import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

class UndertowRawWebSocketClient implements WebSocketRawClient {
    private static final Logger logger = Logger.getLogger(UndertowRawWebSocketClient.class.getName());

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
        logger.log(Level.FINE, "Sending message...");
        final var sent = new CompletableFutureWebSocketCallback();
        WebSockets.sendBinary(ByteBuffer.wrap(message), undertowChannel, sent);
        return sent.getCompletion()
                .whenComplete((r, error) -> {
                    if (error == null) return;
                    try {
                        undertowChannel.close();
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<Void> drop(int reasonCode, String reason) {
        logger.log(Level.FINE, "Closing web socket connection for " + reason + " (" + reasonCode + ")");
        final var sent = new CompletableFutureWebSocketCallback();
        WebSockets.sendClose(reasonCode, reason, undertowChannel, sent);
        return sent.getCompletion().whenComplete(($, $$) -> {
            try {
                undertowChannel.close();
            } catch (IOException e) {
               throw new CompletionException(e);
            }
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
