package com.duncpro.jrest;

import com.duncpro.jrest.util.ConsolidatingByteArraySubscriber;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Immutable
public class SerializedHttpResponse {
    private final Map<String, List<String>> header;
    private final Flow.Publisher<byte[]> body;
    private final int statusCode;

    public SerializedHttpResponse(Map<String, List<String>> header, Flow.Publisher<byte[]> body, int statusCode) {
        this.header = header.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey().toLowerCase(), entry -> List.copyOf(entry.getValue())));
        this.body = body;
        this.statusCode = statusCode;
    }

    public SerializedHttpResponse(int statusCode) {
        this.header = Collections.emptyMap();
        this.statusCode = statusCode;
        this.body = null;
    }

    public Optional<Flow.Publisher<byte[]>> getBody() {
        return Optional.ofNullable(body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "SerializedHttpResponse{" +
                "header=" + header +
                ", body=" + body +
                ", statusCode=" + statusCode +
                '}';
    }
}
