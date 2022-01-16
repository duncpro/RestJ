package com.duncpro.restj;

import com.duncpro.jrest.*;
import com.duncpro.jrest.exceptional.BadRequestException;
import com.duncpro.jroute.HttpMethod;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class SmokeTestApi {
    @HttpEndpoint(HttpMethod.POST)
    @HttpResource(route = "/ping")
    public String handlePingRequest() {
        return "pong";
    }

    @HttpEndpoint(HttpMethod.POST)
    @HttpResource(route = "/greeting")
    public CompletableFuture<String> handleGreetingRequest(HttpRequest request) {
//       return request.getBody().orElseThrow(BadRequestException::new)
//               .thenApply(String::new)
//               .thenApply(name -> "Hello " + name);
        throw new RuntimeException();
    }
}
