package com.duncpro.jrest.integration;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public interface BodyDeserializer {
    <T> CompletableFuture<T> deserialize(TypeLiteral<T> toType, Flow.Publisher<byte[]> serialized);
}
