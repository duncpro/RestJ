package com.duncpro.restj;

import com.duncpro.jrest.HttpRequest;
import com.duncpro.jrest.HttpApi;
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
import java.util.concurrent.SubmissionPublisher;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.runAsync;

public class RestJUndertowHttpHandler implements HttpHandler {
    private final HttpApi httpApi;
    private final Provider<Undertow> server;

    @Inject
    public RestJUndertowHttpHandler(HttpApi restApi, Provider<Undertow> server) {
        this.httpApi = restApi;
        this.server = server;
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

        if (exchange.isUpgrade()) {
            exchange.dispatch(Handlers.websocket((socketExchange, channel) -> {
                channel.setAttribute("sessionId", UUID.randomUUID());
                final var wsClient = new UndertowRawWebSocketClient(request.getHttpMethod(), request.getPath(), channel);
                channel.getCloseSetter().set((ChannelListener<AbstractFramedChannel>) $ -> {
                    httpApi.handleWebSocketClosed(wsClient)
                            .whenComplete(this::catchErrors);
                });
                httpApi.handleWebSocketOpened(request, wsClient)
                        .thenAccept(endpointExists -> {
                            if (endpointExists) return;
                            wsClient.drop(404, "This endpoint does not support web socket protocol.")
                                    .whenComplete(($, error) -> error.printStackTrace());
                        })
                        .whenComplete(this::catchErrors);
            }));
            return;
        }

        httpApi.processRequest(request)
                .thenAccept(response -> {
                    exchange.setStatusCode(response.getStatusCode());
                    final var undertowResponseHeaders = exchange.getResponseHeaders();
                    response.getHeader().forEach((key, values) -> undertowResponseHeaders
                            .addAll(new HttpString(key), values));
                    final var body = response.getBody();
                    if (body.isEmpty()) {
                        exchange.endExchange();
                        return;
                    }
                    body.get().subscribe(new UndertowSenderSubscriber(server.get(), exchange));
                })
                .whenComplete(this::catchErrors);
    }

    private <T> void catchErrors(T returnValue, Throwable error) {
        if (error == null) return;
        error.printStackTrace();
        runAsync(() -> server.get().stop());
    }
}
