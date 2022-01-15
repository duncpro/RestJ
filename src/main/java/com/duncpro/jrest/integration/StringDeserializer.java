package com.duncpro.jrest.integration;

import static java.util.Objects.requireNonNull;

public interface StringDeserializer<T> {
    T deserialize(String serialized);
}
