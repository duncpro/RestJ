package com.duncpro.restj.integration;

public interface StringDeserializer<T> {
    T deserialize(String serialized);
}
