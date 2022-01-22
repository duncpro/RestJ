package com.duncpro.restj.integration;

import com.google.inject.TypeLiteral;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

public interface BodyDeserializer {
    <T> CompletableFuture<T> deserialize(TypeLiteral<T> toType, Flow.Publisher<byte[]> serialized);
}
