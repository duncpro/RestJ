package com.duncpro.jrest.util;

import java.util.concurrent.CompletionException;

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
}
