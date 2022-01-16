package com.duncpro.restj;

import com.duncpro.jrest.HttpRequest;
import com.duncpro.jrest.HttpRestApi;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import io.undertow.Undertow;
import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RestJUndertowHttpHandler implements HttpHandler {
    private final HttpRestApi restApi;
    private final Provider<Undertow> server;

    @Inject
    public RestJUndertowHttpHandler(HttpRestApi restApi, Provider<Undertow> server) {
        this.restApi = restApi;
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

        restApi.processRequest(request)
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
                .whenComplete((response, error) -> {
                    if (error == null) return;
                    error.printStackTrace();
                    server.get().stop();
                });
    }
}
