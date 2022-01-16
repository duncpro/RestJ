package com.duncpro.jrest;

import com.duncpro.jrest.exceptional.BadRequestException;
import com.duncpro.jrest.exceptional.RequestException;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * This interface can optionally be implemented by <i>Composite Request Elements</i>.
 * It provides an async-injection-safe alternative to {@code @PostConstruct}.
 * This class should be used as a replacement for {@code @PostConstruct} when initialization
 * functionality is needed within a <i>Composite Request Element</i>. Either, all, or none, of the methods
 * can be implemented. However, for clarity's sake, implementors are advised to either implement {@link #init()}
 * or {@link #initAsync()}, not both.
 *
 * <i>Composite Request Elements</i> are initialized sequentially, in the order in which they were constructed by Guice.
 * The {@link #init()} method is called immediately before the {@link #initAsync()}. The next <i>Composite Request Element</i>
 * will not be called until {@link #init()} has finished executing normally and the future returned by {@link #initAsync()}
 * has completed successfully. If either of the aforementioned methods fail in some way, the next <i>Composite Request Element</i>
 * will never be initialized and the inbound request will be rejected.
 *
 * The initialization process does not begin until all async injections have been satisfied in all
 * <i>Composite Request Elements</i> in the current request context.
 */
public interface CompositeRequestElement {
    /**
     * This lifecycle method is invoked after all async injections have been satisfied, but before the
     * endpoint handler method which requested this element has been invoked. Implementations may safely
     * throw {@link RequestException}s such as {@link BadRequestException} to prevent invocation of the
     * request handler method and reject the request.
     *
     * This method is called immediately before {@link #initAsync()} is called.
     */
    default void init() {}

    /**
     * This lifecycle method is invoked after all async injections have been satisfied, but before the
     * endpoint handler method which requested this element has been invoked. Implementations may safely
     * return futures which fail with {@link RequestException} such as {@link BadRequestException} to prevent
     * invocation of the request handler method and reject the request.
     *
     * This method is called immediately after {@link #init()} is called.
     *
     * Despite this method's name, this future is executed in sequence with the other <i>Composite Request Elements</i>
     * in the current request context. This method provides support for performing long-running non-blocking operations
     * within the initialization sequence.
     */
    default CompletableFuture<Void> initAsync() { return completedFuture(null); }

}
