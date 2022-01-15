package com.duncpro.jrest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

public class AsyncInjectionOrchestrator {
    private final Set<CompletableFuture<Void>> completions = new HashSet<>();
    private final List<CompositeRequestElement> compositeElements = new ArrayList<>();
    private boolean isWindowClosed = false;

    void registerAsyncInjection(CompletableFuture<Void> completion) {
        if (isWindowClosed) throw new IllegalStateException("Async injection is not available after construction" +
                " of the REST resource class has completed.");

        completions.add(completion);
    }

    CompletableFuture<Void> getCompletion() {
        isWindowClosed = true;
        return CompletableFuture.allOf(completions.toArray(CompletableFuture[]::new));
    }

    void registerPostConstructionHook(CompositeRequestElement element) {
        if (isWindowClosed) throw new IllegalStateException("Async injection is not available after construction" +
                " of the REST resource class has completed.");
        compositeElements.add(element);
    }

    CompletableFuture<Void> callInitializers() {
        CompletableFuture<Void> completion = CompletableFuture.completedFuture(null);
        for (final var element : compositeElements) {
            completion = completion.thenRun(element::init);
            completion = completion.thenCompose($ -> element.initAsync());
        }
        return completion;
    }
}
