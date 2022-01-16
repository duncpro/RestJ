package com.duncpro.restj;

import com.duncpro.jrest.HttpEndpoint;
import com.duncpro.jrest.HttpResource;
import com.duncpro.jroute.HttpMethod;

public class SmokeTestApi {
    @HttpResource(route = "ping")
    @HttpEndpoint(HttpMethod.POST)
    public String handleRequest() {
//        throw new RuntimeException();
        System.out.println("pong");
        return "pong";
    }
}
