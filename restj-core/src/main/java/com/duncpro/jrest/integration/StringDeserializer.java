package com.duncpro.jrest.integration;

public interface StringDeserializer<T> {
    T deserialize(String serialized);
}
