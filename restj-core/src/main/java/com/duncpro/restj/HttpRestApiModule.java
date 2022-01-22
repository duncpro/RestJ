package com.duncpro.restj;

import com.duncpro.restj.integration.HttpIntegrator;
import com.duncpro.jroute.router.Router;
import com.duncpro.jroute.router.TreeRouter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;

import java.util.Set;

public class HttpRestApiModule extends AbstractModule {
    @Override
    public void configure() {
        requireBinding(HttpIntegrator.class);
        bindListener(Matchers.any(), new RequestElementListener());
    }

    @Provides
    @WebSocketEventReceiver(WebSocketEventType.OPENED)
    Router<DeclarativeHttpEndpoint> provideSocketOpenedRouter(Set<DeclarativeHttpEndpoint> endpoints) {
        final var router = new TreeRouter<DeclarativeHttpEndpoint>();
        for (final var endpoint : endpoints) {
            if (endpoint.getReceiverType() != ReceiverType.WEB_SOCKET) continue;
            if (endpoint.getWebSocketEventType() != WebSocketEventType.OPENED) continue;
            router.addRoute(endpoint.getHttpMethod(), endpoint.getRoute().getAbstractRoute(), endpoint);
        }
        return router;
    }

    @Provides
    @WebSocketEventReceiver(WebSocketEventType.CLOSED)
    Router<DeclarativeHttpEndpoint> provideSocketClosedRouter(Set<DeclarativeHttpEndpoint> endpoints) {
        final var router = new TreeRouter<DeclarativeHttpEndpoint>();
        for (final var endpoint : endpoints) {
            if (endpoint.getReceiverType() != ReceiverType.WEB_SOCKET) continue;
            if (endpoint.getWebSocketEventType() != WebSocketEventType.CLOSED) continue;
            router.addRoute(endpoint.getHttpMethod(), endpoint.getRoute().getAbstractRoute(), endpoint);
        }
        return router;
    }

    @Provides
    @RequestReceiver
    Router<DeclarativeHttpEndpoint> providerRequestRouter(Set<DeclarativeHttpEndpoint> endpoints) {
        final var router = new TreeRouter<DeclarativeHttpEndpoint>();
        for (final var endpoint : endpoints) {
            if (endpoint.getReceiverType() != ReceiverType.REST_REQUEST) continue;
            router.addRoute(endpoint.getHttpMethod(), endpoint.getRoute().getAbstractRoute(), endpoint);
        }
        return router;
    }
}
