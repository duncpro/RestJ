package com.duncpro.restj;

import com.duncpro.jroute.HttpMethod;

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
