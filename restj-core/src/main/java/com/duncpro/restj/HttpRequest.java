package com.duncpro.restj;

import com.duncpro.restj.util.ConsolidatingByteArraySubscriber;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Immutable
public class HttpRequest {
    private final Map<String, List<String>> query;
    private final Map<String, List<String>> header;
    private final HttpMethod httpMethod;
    private final Path path;
    private final Flow.Publisher<byte[]> bodyPublisher;

    public HttpRequest(Map<String, List<String>> query, HttpMethod httpMethod, Path path, Map<String, List<String>> header,
                       Flow.Publisher<byte[]> bodyPublisher) {
        this.query = Map.copyOf(query);
        this.httpMethod = requireNonNull(httpMethod);
        this.path = requireNonNull(path);
        this.header = header.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(entry -> entry.getKey().toLowerCase(), entry -> List.copyOf(entry.getValue())));
        this.bodyPublisher = bodyPublisher;
    }

    public Optional<String> getQueryArgument(String queryParameter) {
        return Optional.ofNullable(query.get(queryParameter)).stream()
                .flatMap(List::stream)
                .findFirst();
    }

    public Stream<String> getHeaderEntries(String key) {
        return Optional.ofNullable(header.get(key.toLowerCase())).stream()
                .flatMap(List::stream);
    }

    public Path getPath() {
        return path;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Optional<Flow.Publisher<byte[]>> getBodyPublisher() {
        return Optional.of(bodyPublisher);
    }

    public Optional<String> getContentType() {
        return getHeaderEntry("Content-Type");
    }

    public Set<String> getAcceptableResponseTypes() {
        return getHeaderEntry("Accept").stream()
                .flatMap(value -> Stream.of(value.split(Pattern.quote(";"))))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public Optional<CompletableFuture<byte[]>> getBody() {
        final var bodyPublisher = getBodyPublisher();
        if (bodyPublisher.isEmpty()) return Optional.empty();
        final var subscriber = new ConsolidatingByteArraySubscriber();
        bodyPublisher.get().subscribe(subscriber);
        return Optional.of(subscriber.getFuture());
    }

    public Optional<String> getHeaderEntry(String key) {
        return getHeaderEntries(key).findFirst();
    }
}
