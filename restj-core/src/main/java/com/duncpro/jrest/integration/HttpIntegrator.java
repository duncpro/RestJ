package com.duncpro.jrest.integration;

import com.duncpro.jrest.HttpResponse;
import com.duncpro.jrest.SerializedHttpResponse;
import com.google.inject.TypeLiteral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public interface HttpIntegrator {
    String serializeHeaderArgument(Object arg);
    <T> T deserializeQueryArgument(TypeLiteral<T> toType, String serializedForm);
    <T> T deserializePathArgument(TypeLiteral<T> toType, String serializedForm);
    <T> T deserializeHeaderArgument(TypeLiteral<T> toType, String serializedForm);

    <T> CompletableFuture<T> deserializeRequestBody(String fromContentType, TypeLiteral<T> intoJavaType, Flow.Publisher<byte[]> serializedForm);

    Set<String> getProducableContentTypes();
    Set<String> getConsumableContentTypes();

    class SerializedResponseBody {
        public final String contentType;
        public final Flow.Publisher<byte[]> bytePublisher;

        public SerializedResponseBody(String contentType, Flow.Publisher<byte[]> bytePublisher) {
            this.contentType = requireNonNull(contentType);
            this.bytePublisher = requireNonNull(bytePublisher);
        }
    }

    SerializedResponseBody serializeResponseBody(Set<String> acceptableContentTypes, Object responseBody);

    default SerializedHttpResponse serializeHttpResponse(HttpResponse response, Set<String> acceptableResponseTypes) {
        final Map<String, List<String>> serializedUserHeader = response.getHeader().entrySet().stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::serializeHeaderArgument)
                                .collect(Collectors.toList()))
                );

        final var serializedBody = response.getBody()
                .map(deserialized -> serializeResponseBody(acceptableResponseTypes, deserialized));

        final var bodyContentType = serializedBody.map(body -> body.contentType);
        final var serializedBodyContent = serializedBody.map(body -> body.bytePublisher).orElse(null);

        final Map<String, List<String>> completeHeader = new HashMap<>();
        bodyContentType.ifPresent(ct -> completeHeader.put("Content-Type", List.of(ct)));
        completeHeader.putAll(serializedUserHeader);

        return new SerializedHttpResponse(completeHeader, serializedBodyContent, response.getStatusCode());
    }
}
