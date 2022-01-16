package com.duncpro.jrest;

import com.duncpro.jrest.exceptional.BadRequestException;
import com.duncpro.jroute.HttpMethod;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@HttpResource(route = "/greeting/{name}")
public class SmokeTestRestApi {

    @Path("name")
    String name;

    @RequestBody
    int n;

    @HttpEndpoint(HttpMethod.PUT)
    int testPut() {
       return n;
    }
}
