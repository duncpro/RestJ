package com.duncpro.jrest;

import com.duncpro.jrest.integration.HttpIntegrator;
import com.duncpro.jroute.router.TreeRouter;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class HttpRestApiModule extends AbstractModule {
    @Override
    public void configure() {
        requireBinding(HttpIntegrator.class);
        bindListener(Matchers.any(), new RequestElementListener());
    }

    @Provides
    @Singleton
    private HttpRestApi provideRestApi(Set<DeclarativeHttpEndpoint> endpoints) {
        final var router = new TreeRouter<DeclarativeHttpEndpoint>();
        for (final var endpoint : endpoints) {
            router.addRoute(endpoint.getHttpMethod(), endpoint.getRoute().getAbstractRoute(), endpoint);
        }
        return new HttpRestApi(router);
    }
}
