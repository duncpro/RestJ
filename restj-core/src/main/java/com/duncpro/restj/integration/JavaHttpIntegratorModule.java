package com.duncpro.restj.integration;

import com.duncpro.restj.exceptional.BadRequestException;
import com.duncpro.restj.util.ByteArrayPublisher;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class JavaHttpIntegratorModule extends AbstractModule {
    @Override
    public void configure() {
        final var deserializers = new UniversalDeserializerBinder(binder());
        deserializers.bind(Integer.class, serialized -> {
            try {
                return Integer.parseInt(serialized);
            } catch (NumberFormatException e) {
                throw new BadRequestException(e);
            }
        });

        deserializers.bind(Double.class, serialized -> {
            try {
                return Double.parseDouble(serialized);
            } catch (NumberFormatException e) {
                throw new BadRequestException(e);
            }
        });

        deserializers.bind(Long.class, serialized -> {
            try {
                return Long.parseLong(serialized);
            } catch (NumberFormatException e) {
                throw new BadRequestException(e);
            }
        });

        deserializers.bind(Float.class, serialized -> {
            try {
                return Float.parseFloat(serialized);
            } catch (NumberFormatException e) {
                throw new BadRequestException(e);
            }
        });

        deserializers.bind(Boolean.class, Boolean::parseBoolean);
        deserializers.bind(String.class, String::toString);
        deserializers.bind(UUID.class, UUID::fromString);

        bind(HttpIntegrator.class).to(DynamicHttpIntegrator.class);
    }

    @Provides
    @ContentType("text/plain")
    BodySerializer providePlainTextSerializer() {
        return body -> new ByteArrayPublisher(body.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Provides
    @HeaderIntegrator
    StringSerializer provideHeaderSerializer() {
        return String::valueOf;
    }
}
