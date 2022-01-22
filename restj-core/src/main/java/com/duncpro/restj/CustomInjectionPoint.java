package com.duncpro.restj;

import com.duncpro.restj.exceptional.BadRequestException;
import com.duncpro.restj.integration.HttpIntegrator;
import com.google.inject.TypeLiteral;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

class CustomInjectionPoint {
    private static final Set<Class<? extends Annotation>> CUSTOM_INJECTION_ANNOTATIONS = Set.of(Query.class, Header.class, Path.class, RequestBody.class);

    final AnnotatedElement element;

    private CustomInjectionPoint(AnnotatedElement element) {
        this.element = requireNonNull(element);
    }

    private static CustomInjectionPoint ofInternal(AnnotatedElement field) {
        if (!isCustomInjectionPoint(field)) throw new IllegalArgumentException();

       return new CustomInjectionPoint(field);
    }

    public static boolean isCustomInjectionPoint(AnnotatedElement element) {
        return Arrays.stream(element.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(CUSTOM_INJECTION_ANNOTATIONS::contains);
    }

    public static CustomInjectionPoint of(Field field) {
        return ofInternal(field);
    }

    public static CustomInjectionPoint of(Parameter parameter) {
        return ofInternal(parameter);
    }

    private TypeLiteral<?> getType() {
        if (element instanceof Parameter) {
            return TypeLiteral.get(((Parameter) element).getType());
        }
        if (element instanceof Field) {
            return TypeLiteral.get(((Field) element).getGenericType());
        }
        throw new AssertionError();
    }

    CompletableFuture<?> resolveValue(HttpRequest request, HttpIntegrator integrator, ParameterizableRoute route) {
        final var isOptional = element.isAnnotationPresent(Nullable.class);

        if (element.isAnnotationPresent(Query.class)) {
            final var queryAnnotation = element.getAnnotation(Query.class);

            final Optional<String> serializedValue = request.getQueryArgument(queryAnnotation.value());
            if (serializedValue.isEmpty() && !isOptional) return failedFuture(new BadRequestException());
            final var deserializedValue = serializedValue.map(v -> integrator.deserializeQueryArgument(getType(), v))
                    .orElse(null);
            return completedFuture(deserializedValue);
        }

        if (element.isAnnotationPresent(Header.class)) {
            final var headerAnnotation = element.getAnnotation(Header.class);
            final Optional<String> serializedValue = request.getHeaderEntry(headerAnnotation.value());
            if (serializedValue.isEmpty() && !isOptional) return failedFuture(new BadRequestException());
            final var deserializedValue = serializedValue.map(v -> integrator.deserializeHeaderArgument(getType(), v))
                    .orElse(null);
            return completedFuture(deserializedValue);
        }

        if (element.isAnnotationPresent(Path.class)) {
            final var pathParam = element.getAnnotation(Path.class).value();
            final var pathArg = route.getVariableElementValue(request.getPath(), pathParam);
            if (pathArg.isEmpty() && !isOptional) return failedFuture(new BadRequestException());
            final var serializedPathArg = pathArg.map(a -> integrator.deserializePathArgument(getType(), a))
                    .orElse(null);
            return completedFuture(serializedPathArg);
        }

        if (element.isAnnotationPresent(RequestBody.class)) {
            if (request.getBodyPublisher().isEmpty() && !isOptional) return failedFuture(new UnsupportedOperationException());
            final var contentType = request.getContentType();
            if (contentType.isEmpty()) return failedFuture(new BadRequestException());
            return request.getBodyPublisher().map(p -> integrator.deserializeRequestBody(contentType.get(), getType(), p))
                    .orElse(CompletableFuture.completedFuture(null));
        }

        throw new IllegalArgumentException();
    }
}
