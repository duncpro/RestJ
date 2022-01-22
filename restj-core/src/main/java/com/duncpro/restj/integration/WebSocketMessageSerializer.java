package com.duncpro.restj.integration;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface WebSocketMessageSerializer extends Function<Object, byte[]> {
}
