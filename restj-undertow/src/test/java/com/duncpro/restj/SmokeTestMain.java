package com.duncpro.restj;

import com.duncpro.jrest.HttpRestApiModule;
import com.duncpro.jrest.integration.JavaHttpIntegratorModule;
import com.google.inject.Guice;
import io.undertow.Undertow;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.Timer;
import java.util.TimerTask;

import static java.util.Objects.requireNonNull;

public class SmokeTestMain {
    public static void main(String[] args) {
        final var serverConfig = Undertow.builder()
                .addHttpListener(8080, "localhost");
        final var injector = Guice.createInjector(new RestJUndertowModule(serverConfig),
                new SmokeTestModule(), new JavaHttpIntegratorModule());

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                injector.getInstance(Undertow.class).stop();
//            }
//        }, 1000 * 10);
        injector.getInstance(Undertow.class).start();
    }
}
