package com.duncpro.jrest.integration;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

public class UniversalDeserializerBinder {
    private final Binder binder;

    public UniversalDeserializerBinder(Binder binder) {
        this.binder = binder;
    }

    public <T> void bind(TypeLiteral<T> type, StringDeserializer<T> deserializer) {
        bindQuery(type, deserializer);
        bindHeader(type, deserializer);
        bindPath(type, deserializer);
    }

    public <T> void bind(Class<T> type, StringDeserializer<T> deserializer) {
        final var typeLiteral = TypeLiteral.get(type);
        bindQuery(typeLiteral, deserializer);
        bindHeader(typeLiteral, deserializer);
        bindPath(typeLiteral, deserializer);
    }

    @SuppressWarnings("unchecked")
    private <T> void bindQuery(TypeLiteral<T> type, StringDeserializer<T> deserializer) {
        final var deserializerType = Types.newParameterizedType(StringDeserializer.class, type.getType());
        final Key<StringDeserializer<T>> key = (Key<StringDeserializer<T>>)
                Key.get(deserializerType, QueryArgIntegrator.class);

        binder.bind(key).toInstance(deserializer);
    }

    @SuppressWarnings("unchecked")
    private <T> void bindHeader(TypeLiteral<T> type, StringDeserializer<T> deserializer) {
        final var deserializerType = Types.newParameterizedType(StringDeserializer.class, type.getType());
        final Key<StringDeserializer<T>> key = (Key<StringDeserializer<T>>)
                Key.get(deserializerType, HeaderIntegrator.class);

        binder.bind(key).toInstance(deserializer);
    }

    @SuppressWarnings("unchecked")
    private <T> void bindPath(TypeLiteral<T> type, StringDeserializer<T> deserializer) {
        final var deserializerType = Types.newParameterizedType(StringDeserializer.class, type.getType());
        final Key<StringDeserializer<T>> key = (Key<StringDeserializer<T>>)
                Key.get(deserializerType, PathArgIntegrator.class);

        binder.bind(key).toInstance(deserializer);
    }
}
