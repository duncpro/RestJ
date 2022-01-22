package com.duncpro.restj;

import com.duncpro.restj.integration.HttpIntegrator;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class RequestElementListener implements TypeListener {
    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        if (CompositeRequestElement.class.isAssignableFrom(type.getRawType())) {
            final var asyncInjectOrchestrator = encounter.getProvider(AsyncInjectionOrchestrator.class);
            encounter.register((InjectionListener<I>) injectee -> {
                asyncInjectOrchestrator.get().registerPostConstructionHook((CompositeRequestElement) injectee);
            });
        }

        for (final var field : type.getRawType().getDeclaredFields()) {
            if (!CustomInjectionPoint.isCustomInjectionPoint(field)) continue;
            final var injectionPoint = CustomInjectionPoint.of(field);

            final var request = encounter.getProvider(HttpRequest.class);
            final var route = encounter.getProvider(ParameterizableRoute.class);
            final var integrator = encounter.getProvider(HttpIntegrator.class);
            final var asyncInjectOrchestrator = encounter.getProvider(AsyncInjectionOrchestrator.class);

            final Supplier<CompletableFuture<?>> valueSupplier = () ->
                    injectionPoint.resolveValue(request.get(), integrator.get(), route.get());

            final var injector = new AsyncMembersInjector<>((Field) injectionPoint.element, valueSupplier,
                    asyncInjectOrchestrator);

            encounter.register(injector);
        }
    }
}
