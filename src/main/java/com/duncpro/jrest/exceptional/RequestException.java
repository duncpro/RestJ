package com.duncpro.jrest.exceptional;

import com.duncpro.jrest.HttpResponse;

import static java.util.Objects.requireNonNull;

public abstract class RequestException extends RuntimeException {
    public RequestException(Throwable cause) {
        super(cause);
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException() {
        super();
    }

    /**
     * Returns an {@link HttpResponse} which represents this {@link RequestException}.
     * {@link RequestException}s thrown within sync handler methods will be converted to responses.
     * Likewise responses will be generated for async handler method invocations which result in a future that fails
     * with {@link RequestException}.
     */
    public abstract HttpResponse getResponse();
}
