package com.duncpro.jrest;

import com.duncpro.jrest.exceptional.NotFoundException;
import com.duncpro.jroute.router.Router;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.failedFuture;

public class HttpRestApi {
    private final Router<DeclarativeHttpEndpoint> router;

    HttpRestApi(Router<DeclarativeHttpEndpoint> router) {
        this.router = requireNonNull(router);
    }

    public CompletableFuture<SerializedHttpResponse> processRequest(HttpRequest request) {
        final var routerResult = this.router.route(request.getHttpMethod(), request.getPath());

        if (routerResult.isEmpty()) return failedFuture(new NotFoundException());

        return routerResult.get().getEndpoint().processRequest(request);
    }
}
