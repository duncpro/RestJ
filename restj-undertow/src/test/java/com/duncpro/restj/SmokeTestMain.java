package com.duncpro.restj;

import com.duncpro.restj.integration.JavaHttpIntegratorModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import io.undertow.Undertow;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public class SmokeTestMain {
    public static void main(String[] args) {
        Logger.getLogger(UndertowRawWebSocketClient.class.getName()).setLevel(Level.ALL);
        Logger.getLogger(HttpApi.class.getName()).setLevel(Level.ALL);
        Logger.getLogger(RestJUndertowHttpHandler.class.getName()).setLevel(Level.ALL);
        Arrays.stream(Logger.getLogger("").getHandlers()).forEach(handler -> handler.setLevel(Level.ALL));

        final var serverConfig = Undertow.builder()
                .setIoThreads(1)
                .setWorkerThreads(0)
                .addHttpListener(8080, "localhost");

        final var injector = Guice.createInjector(Stage.DEVELOPMENT, new RestJUndertowModule(serverConfig),
                new SmokeTestModule(), new JavaHttpIntegratorModule());

        injector.getInstance(Undertow.class).start();
    }

}
