package com.duncpro.jrest.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class FutureUtils {
    public static Throwable unwrapCompletionException(Throwable e) {
        if (e instanceof CompletionException) {
            return unwrapCompletionException(e.getCause());
        }

        return e;
    }

    public static CompletionException wrapException(Throwable e) {
        if (e instanceof CompletionException) return (CompletionException) e;
        return new CompletionException(e);
    }

    public static <T> BiFunction<T, Throwable, Void> catchIOExceptions(Consumer<IOException> exceptionConsumer) {
        return (result, error) -> {
            if (error == null) return null;
            var cause = unwrapCompletionException(error);

            if (cause instanceof UncheckedIOException) cause = ((UncheckedIOException) cause).getCause();

            if (cause instanceof IOException) {
                exceptionConsumer.accept((IOException) cause);
                return null;
            } else {
                throw new CompletionException(cause);
            }
        };
    }
}
