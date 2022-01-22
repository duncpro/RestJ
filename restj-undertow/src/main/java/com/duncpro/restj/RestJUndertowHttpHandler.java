package com.duncpro.restj;

import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.protocol.framed.AbstractFramedChannel;
import io.undertow.util.HttpString;
import org.xnio.ChannelListener;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

public class RestJUndertowHttpHandler implements HttpHandler {
    private final HttpApi httpApi;
    private final Provider<Undertow> server;
    private final Logger logger;

    @Inject
    public RestJUndertowHttpHandler(HttpApi restApi, Provider<Undertow> server, Logger logger) {
        this.httpApi = requireNonNull(restApi);
        this.server = requireNonNull(server);
        this.logger = requireNonNull(logger);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final var headers = new HashMap<String, List<String>>();
        final var method = HttpMethod.valueOf(exchange.getRequestMethod().toString());
        final var path = new Path(exchange.getRequestPath());
        final var query = new HashMap<String, List<String>>();

        exchange.getRequestHeaders()
                .forEach(header -> headers.put(header.getHeaderName().toString(), new ArrayList<>(header)));

        exchange.getQueryParameters().forEach((param, values) -> {
            query.put(param, new ArrayList<>(values));
        });

        HttpRequest request;
        {
            final var bodyPublisher = new SubmissionPublisher<byte[]>();
            final Receiver.PartialBytesCallback dataCallback = ($, partialContent, isFinalPacket) -> {
                bodyPublisher.submit(partialContent);
                if (isFinalPacket) bodyPublisher.close();
            };
            final Receiver.ErrorCallback errorCallback = ($, error) -> {
                bodyPublisher.closeExceptionally(error);
            };
            exchange.getRequestReceiver().receivePartialBytes(dataCallback, errorCallback);
            request = new HttpRequest(query, method, path, headers, bodyPublisher);
        }

        final var isUpgrade = request.getHeaderEntry("Upgrade").stream()
                .map(entry -> entry.equals("websocket"))
                .findFirst()
                .orElse(false);

        if (isUpgrade) {
            logger.log(Level.FINE, "Received upgrade protocol request: " + request);
            final var webSocketHandler = Handlers.websocket((socketExchange, channel) -> {
                logger.log(Level.FINE, "Opened new web socket connection: " + request);
                channel.setAttribute("sessionId", UUID.randomUUID());
                final var wsClient = new UndertowRawWebSocketClient(request.getHttpMethod(), request.getPath(), channel);
                channel.getCloseSetter().set((ChannelListener<AbstractFramedChannel>) $ -> {
                    httpApi.handleWebSocketClosed(wsClient)
                            .handle(this::catchErrors);
                });
                httpApi.handleWebSocketOpened(request, wsClient)
                        .thenAccept(endpointExists -> {
                            if (endpointExists) return;
                            wsClient.drop(404, "This endpoint does not support web socket protocol.")
                                    .whenComplete(($, error) -> error.printStackTrace());
                        })
                        .handle(this::catchErrors);
            });
            webSocketHandler.handleRequest(exchange);
            return;
        }

        final var requestProcessed = httpApi.processRequest(request)
                .thenCompose(response -> {
                    exchange.setStatusCode(response.getStatusCode());
                    final var undertowResponseHeaders = exchange.getResponseHeaders();
                    response.getHeader().forEach((key, values) -> undertowResponseHeaders
                            .addAll(new HttpString(key), values));
                    final var body = response.getBody();
                    CompletableFuture<?> bodySent = completedFuture(null);

                    if (body.isPresent()) {
                        final var subscriber = new UndertowSenderSubscriber(exchange);
                        body.get().subscribe(subscriber);
                        bodySent = subscriber.getCompletion();
                    }

                    return bodySent;
                });

        requestProcessed
                .whenComplete(($, $$) -> exchange.getResponseSender().close())
                .handle(this::catchErrors);
    }

    private <T> CompletableFuture<Void> catchErrors(T returnValue, Throwable error) {
        if (error != null) {
            error.printStackTrace();
            runAsync(() -> server.get().stop());
        }

        return CompletableFuture.completedFuture(null);
    }
}
