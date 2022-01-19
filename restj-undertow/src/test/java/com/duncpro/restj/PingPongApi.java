package com.duncpro.restj;

import com.duncpro.jrest.*;
import com.duncpro.jrest.util.FutureUtils;
import com.duncpro.jroute.HttpMethod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.duncpro.jrest.util.FutureUtils.catchIOExceptions;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.delayedExecutor;

@HttpResource(route = "/ping")
@HttpEndpoint(HttpMethod.GET)
class PingPongApi {

    @RequestReceiver
    String handlePingRequest() {
        System.out.println("Received normal ping");
        return "pong";
    }

    @WebSocketEventReceiver(WebSocketEventType.OPENED)
    CompletableFuture<Void> startPongSubscription(WebSocketClient client) {
        return client.sendMessage("pong")
                .thenComposeAsync($ -> client.sendMessage("1"), delayedExecutor(1, TimeUnit.SECONDS))
                .thenComposeAsync($ -> client.sendMessage("2"), delayedExecutor(1, TimeUnit.SECONDS))
                .thenComposeAsync($ -> client.sendMessage("3"), delayedExecutor(1, TimeUnit.SECONDS))
                .thenCompose($ -> client.drop(200, "The pong has been sent"))
                .handle(catchIOExceptions(Throwable::printStackTrace)); // Important!
                // Uncaught IOExceptions WILL crash all the default implementations (purposefully).
    }

    @WebSocketEventReceiver(WebSocketEventType.CLOSED)
    void stopPongSubscription(WebSocketClient client) {
        System.out.println("stopped");
    }

}
