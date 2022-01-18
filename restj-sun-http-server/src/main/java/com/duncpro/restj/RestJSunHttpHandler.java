package com.duncpro.restj;

import com.duncpro.jrest.HttpRequest;
import com.duncpro.jrest.HttpApi;
import com.duncpro.jrest.SerializedHttpResponse;
import com.duncpro.jrest.util.URLUtils;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.SubmissionPublisher;

import static java.util.Objects.requireNonNull;

public class RestJSunHttpHandler implements HttpHandler {
    private final HttpApi restApi;
    private final HttpServer server;

    public RestJSunHttpHandler(HttpApi restApi, HttpServer server) {
        this.restApi = requireNonNull(restApi);
        this.server = requireNonNull(server);
    }

    @Override
    public void handle(HttpExchange exchange) {
        final var path = new Path(exchange.getRequestURI().getPath());
        final var method = HttpMethod.valueOf(exchange.getRequestMethod());
        final var header = exchange.getRequestHeaders();
        final var queryParams = URLUtils.parseQueryParams(exchange.getRequestURI().getQuery());

        final var isChunked = Optional.ofNullable(header.get("Transfer-Encoding"))
                .stream()
                .flatMap(List::stream)
                .map(encoding -> Objects.equals(encoding, "chunked"))
                .findFirst()
                .orElse(false);

        final var requestHasBody = Optional.ofNullable(exchange.getRequestHeaders().get("Content-Length"))
                .stream()
                .flatMap(List::stream)
                .findFirst()
                .map(Integer::parseInt)
                .map(length -> length != 0)
                .orElse(isChunked);

        final var requestBodyPublisher = new SubmissionPublisher<byte[]>();
        final var request = new HttpRequest(queryParams, method, path, header, requestHasBody ? requestBodyPublisher : null);
        final var responseFuture = restApi.processRequest(request);

        if (requestHasBody) {
            try (requestBodyPublisher; final var inputStream = exchange.getRequestBody()) {
                int data;
                while ((data = inputStream.read()) != -1) {
                    requestBodyPublisher.submit(new byte[] { ((byte) data) });
                }
            } catch (IOException e) {
                requestBodyPublisher.closeExceptionally(e);
            }
        }

        final SerializedHttpResponse response;

        try {
            response = responseFuture.join();
        } catch (CompletionException e) {
            e.printStackTrace();
            server.stop(0);
            return;
        }

        exchange.getResponseHeaders().putAll(response.getHeader());
        try {
            exchange.sendResponseHeaders(response.getStatusCode(), response.getBody().isPresent() ? 0 : -1);
        } catch (IOException e) {
            e.printStackTrace();
            exchange.close();
            return;
        }

        try {
            if (response.getBody().isPresent()) {
                final var responseSubscriber = new OutputStreamSubscriber(exchange.getResponseBody());
                response.getBody().get().subscribe(responseSubscriber);
                try {
                    responseSubscriber.getCompletion().join();
                } catch (CompletionException e) {
                    if (e.getCause() instanceof IOException) {
                        e.getCause().printStackTrace();
                    } else {
                        e.printStackTrace();
                        server.stop(0);
                    }
                }
            }
        } finally {
            exchange.close();
        }
    }
}
