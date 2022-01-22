package com.duncpro.restj;

import com.google.inject.MembersInjector;

import javax.inject.Provider;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

class AsyncMembersInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final Supplier<CompletableFuture<?>> supplier;
    private final Provider<AsyncInjectionOrchestrator> asyncInjectOrchestrator;

    AsyncMembersInjector(Field field, Supplier<CompletableFuture<?>> supplier,
                         Provider<AsyncInjectionOrchestrator> asyncInjectOrchestrator) {
        this.field = requireNonNull(field);
        this.supplier = requireNonNull(supplier);
        this.asyncInjectOrchestrator = requireNonNull(asyncInjectOrchestrator);
    }

    @Override
    public void injectMembers(T instance) {
        final var injected = supplier.get().thenAccept(fieldValue -> {
            field.setAccessible(true);
            try {
                field.set(instance, fieldValue);
            } catch (IllegalAccessException e) {
                throw new CompletionException(e);
            }
        });
        asyncInjectOrchestrator.get().registerAsyncInjection(injected);
    }
}
