package com.duncpro.jrest.integration;

import com.duncpro.jrest.exceptional.ContentNegotiationException;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

public class DynamicHttpIntegrator implements HttpIntegrator {
    private final Injector injector;

    @Inject
    public DynamicHttpIntegrator(Injector injector) {
        this.injector = injector;
    }

    @Override
    public String serializeHeaderArgument(Object arg) {
        final var key = Key.get(StringSerializer.class, HeaderIntegrator.class);
        return injector.getInstance(key).serialize(arg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeQueryArgument(TypeLiteral<T> toType, String serializedForm) {
        final var deserializerType = Types.newParameterizedType(StringDeserializer.class, toType.getType());
        final var key = Key.get(deserializerType, QueryArgIntegrator.class);
        return ((StringDeserializer<T>) injector.getInstance(key)).deserialize(serializedForm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializePathArgument(TypeLiteral<T> toType, String serializedForm) {
        final var deserializerType = Types.newParameterizedType(StringDeserializer.class, toType.getType());
        final var key = Key.get(deserializerType, PathArgIntegrator.class);
        return ((StringDeserializer<T>) injector.getInstance(key)).deserialize(serializedForm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeHeaderArgument(TypeLiteral<T> toType, String serializedForm) {
        final var deserializerType = Types.newParameterizedType(StringDeserializer.class, toType.getType());
        final var key = Key.get(deserializerType, HeaderIntegrator.class);
        return ((StringDeserializer<T>) injector.getInstance(key)).deserialize(serializedForm);
    }

    @Override
    public SerializedResponseBody serializeResponseBody(Set<String> acceptableContentTypes, Object responseBody) {
        final var serializerBinding = acceptableContentTypes.stream()
                .map(contentType -> injector.getBindings().get(Key.get(BodySerializer.class, ContentTypes.get(contentType))))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ContentNegotiationException(acceptableContentTypes, getProducableContentTypes()));

        final BodySerializer serializer = (BodySerializer) serializerBinding.getProvider().get();
        final String contentType = ((ContentType) serializerBinding.getKey().getAnnotation()).value();

        return new SerializedResponseBody(contentType, serializer.serialize(responseBody));
    }

    @Override
    public <T> CompletableFuture<T> deserializeRequestBody(String fromContentType, TypeLiteral<T> intoJavaType, Flow.Publisher<byte[]> serializedForm) {
        final var key = Key.get(BodyDeserializer.class, ContentTypes.get(fromContentType));

        final BodyDeserializer deserializer;
        try {
            deserializer = injector.getProvider(key).get();
        } catch (ConfigurationException e) {
            return CompletableFuture.failedFuture(new ContentNegotiationException(Set.of(fromContentType), getConsumableContentTypes()));
        }
        return deserializer.deserialize(intoJavaType, serializedForm);
    }

    @Override
    public Set<String> getProducableContentTypes() {
        return injector.getBindings().keySet().stream()
                .filter(key -> BodySerializer.class.isAssignableFrom(key.getTypeLiteral().getRawType()))
                .filter(key -> key.getAnnotation() instanceof ContentType)
                .map(key -> ((ContentType) key.getAnnotation()).value())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getConsumableContentTypes() {
        return injector.getBindings().keySet().stream()
                .filter(key -> BodyDeserializer.class.isAssignableFrom(key.getTypeLiteral().getRawType()))
                .filter(key -> key.getAnnotation() instanceof ContentType)
                .map(key -> ((ContentType) key.getAnnotation()).value())
                .collect(Collectors.toSet());
    }

}
