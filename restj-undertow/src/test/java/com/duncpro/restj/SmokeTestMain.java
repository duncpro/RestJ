package com.duncpro.restj;

import com.duncpro.jrest.HttpApi;
import com.duncpro.jrest.HttpRestApiModule;
import com.duncpro.jrest.integration.JavaHttpIntegratorModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import io.undertow.Undertow;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Handler;
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
