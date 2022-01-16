package com.duncpro.jrest.util;

import java.util.concurrent.CompletionException;

public class FutureUtils {
    public static Throwable unwrapCompletionException(Throwable e) {
        if (e instanceof CompletionException) {
            return unwrapCompletionException(e.getCause());
        }

        return e;
    }
}
