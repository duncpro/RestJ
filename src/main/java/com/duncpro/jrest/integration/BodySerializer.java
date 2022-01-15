package com.duncpro.jrest.integration;

import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public interface BodySerializer {
    Flow.Publisher<byte[]> serialize(Object body);
}
