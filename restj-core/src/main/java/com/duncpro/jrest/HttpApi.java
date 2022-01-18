package com.duncpro.jrest;

import com.duncpro.jrest.exceptional.NotFoundException;
import com.duncpro.jroute.router.Router;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class HttpApi {
    @Inject
    @WebSocketEventReceiver(WebSocketEventType.CLOSED)
    private Router<DeclarativeHttpEndpoint> socketClosedRouter;

    @Inject
    @WebSocketEventReceiver(WebSocketEventType.OPENED)
    private Router<DeclarativeHttpEndpoint> socketOpenedRouter;

    @Inject
    @RequestReceiver
    private Router<DeclarativeHttpEndpoint> requestRouter;

    public CompletableFuture<Boolean> handleWebSocketOpened(HttpRequest request, WebSocketRawClient client) {
        final var result = socketOpenedRouter.route(request.getHttpMethod(), request.getPath());
        if (result.isEmpty()) return completedFuture(false);
        return result.get().getEndpoint().processWebSocketOpened(request, client)
                .thenApply($ -> true);
    }

    public CompletableFuture<Void> handleWebSocketClosed(WebSocketRawClient client) {
        final var result = socketClosedRouter.route(client.getEndpointMethod(), client.getEndpointPath())
                .orElseThrow(IllegalStateException::new);

        return result.getEndpoint().processWebSocketClosed(client);
    }

    public CompletableFuture<SerializedHttpResponse> processRequest(HttpRequest request) {
        final var routerResult = requestRouter.route(request.getHttpMethod(), request.getPath());
        if (routerResult.isEmpty()) return completedFuture(new NotFoundException().getSerializedResponse());
        return routerResult.get().getEndpoint().processRequest(request);
    }
}
