package com.duncpro.restj;

import com.duncpro.jrest.HttpEndpoint;
import com.duncpro.jrest.HttpResource;
import com.duncpro.jroute.HttpMethod;
import io.undertow.Undertow;
import io.undertow.server.handlers.GracefulShutdownHandler;

import javax.inject.Inject;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class SmokeTestApi {
    @HttpResource(route = "ping")
    @HttpEndpoint(HttpMethod.POST)
    public String handleRequest() {
        System.out.println("pong");
        return "pong";
    }
}
