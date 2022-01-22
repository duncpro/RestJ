package com.duncpro.restj;

import com.duncpro.restj.integration.JavaHttpIntegratorModule;
import com.google.inject.Guice;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

public class SmokeTestMain {
    public static void main(String[] args) throws IOException {
        final var injector = Guice.createInjector(new SmokeTestModule(), new JavaHttpIntegratorModule(), new HttpRestApiModule());
        final var restApi = injector.getInstance(HttpApi.class);
        final var server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        final var threadPool = Executors.newCachedThreadPool();
        server.setExecutor(threadPool);
        server.createContext("/", new RestJSunHttpHandler(restApi, server));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)) {});
        server.start();
    }
}
