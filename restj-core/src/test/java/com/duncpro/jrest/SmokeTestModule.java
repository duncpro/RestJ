package com.duncpro.jrest;

import com.duncpro.jrest.exceptional.InternalServerErrorException;
import com.duncpro.jrest.integration.BodyDeserializer;
import com.duncpro.jrest.integration.BodySerializer;
import com.duncpro.jrest.integration.ContentType;
import com.duncpro.jrest.integration.JavaHttpIntegratorModule;
import com.duncpro.jrest.util.ByteArrayPublisher;
import com.duncpro.jrest.util.ConsolidatingByteArraySubscriber;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public class SmokeTestModule extends AbstractModule {
    @Override
    protected void configure() {
        // This module binds the class HttpRestApi.
        // That class can be used to generate outbound responses from inbound requests using the declarative HTTP
        // endpoints which exist within the injector which is installing this module
        install(new HttpRestApiModule());

        // This module provides header, query param, path param, deserialization support for standard Java data types
        // like Long, Boolean, Integer, Double, String, etc.
        // Additionally this module provides a body serializer for the Content-Type text/plain which generates
        // response bodies using Object#toString.
        install(new JavaHttpIntegratorModule());

        // HttpRestApiEndpointBinder is used to bind HTTP resource classes.
        // Any class which declares an @HttpEndpoint annotated method should be bound using this binder.
        // Resource classes may not be Singleton scoped. Instead a new instance is created for each inbound request.
        final var endpointBinder = new HttpRestApiEndpointBinder(binder());

        // Bind the single resource class for this smoke test.
        endpointBinder.bind(SmokeTestRestApi.class);
    }

    // Provides a BodySerializer which is used to convert the bodies of inbound requests which have a
    // Content-Type of application/json to Java objects. This implementation leverages Jackson, but any library
    // might be used.
    @Provides
    @ContentType("application/json")
    BodySerializer provideJsonBodySerializationSupport(ObjectMapper jackson) {
        return body -> {
            final byte[] serialized;
            try {
                serialized = jackson.writeValueAsBytes(body);
            } catch (JsonProcessingException e) {
                throw new InternalServerErrorException(e);
            }
           return new ByteArrayPublisher(serialized);
        };
    }

    // Provides a BodyDeserializer which is used to convert the bodies of outbound responses to JSON if
    // the request which generated the response has an Accept header of application/json.
    // This implementation leverages Jackson, but any library might be used.
    @Provides
    @ContentType("application/json")
    BodyDeserializer provideJsonBodyDeserializationSupport(ObjectMapper jackson) {
        return new BodyDeserializer() {
            @Override
            public <T> CompletableFuture<T> deserialize(TypeLiteral<T> toType, Flow.Publisher<byte[]> serializedStream) {
                final var subscriber = new ConsolidatingByteArraySubscriber();
                serializedStream.subscribe(subscriber);
                final var jacksonType = jackson.constructType(toType.getType());
                return subscriber.getFuture().thenApply(serializedData -> {
                    try {
                        return jackson.readValue(serializedData, jacksonType);
                    } catch (IOException e) {
                        throw new InternalServerErrorException(e);
                    }
                });
            }
        };
    }
}
