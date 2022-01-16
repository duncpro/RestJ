package com.duncpro.jrest;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Immutable
public class HttpResponse {
    private final Object body;
    private final int statusCode;
    private final Map<String, List<Object>> header;

    public HttpResponse(int statusCode) {
        this(Map.of(), null, statusCode);
    }

    public HttpResponse(Map<String, List<Object>> header, Object body, int statusCode) {
        this.header = header.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
        this.body = body;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Optional<Object> getBody() {
        return Optional.ofNullable(body);
    }

    public Map<String, List<Object>> getHeader() {
        return header;
    }
}
