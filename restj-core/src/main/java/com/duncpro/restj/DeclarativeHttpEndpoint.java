package com.duncpro.restj;

import com.duncpro.restj.exceptional.ContentNegotiationException;
import com.duncpro.restj.exceptional.RequestException;
import com.duncpro.restj.integration.HttpIntegrator;
import com.duncpro.restj.util.FutureUtils;
import com.duncpro.jroute.HttpMethod;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.duncpro.restj.util.FutureUtils.unwrapCompletionException;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

class DeclarativeHttpEndpoint {
    private final Method method;
    private final Provider<Injector> applicationInjector;

    DeclarativeHttpEndpoint(Method method, Provider<Injector> applicationInjector) {
        this.method = requireNonNull(method);
        this.applicationInjector = requireNonNull(applicationInjector);
    }

    private HttpResponse mapInvocationResultToResponse(Object returnValue, Throwable e) {
        if (e != null) {
            e = unwrapCompletionException(e);

            if (e instanceof RequestException) {
                return ((RequestException) e).getResponse();
            }

            throw new CompletionException(e);
        }

        if (returnValue instanceof HttpResponse) {
            return (HttpResponse) returnValue;
        } else {
            // Content-Type header will be applied during serialization
            return new HttpResponse(Map.of(), returnValue, 200);
        }
    }

    private SerializedHttpResponse handleContentNegotiationFailure(SerializedHttpResponse response, Throwable e) {
        if (e != null) {
            e = unwrapCompletionException(e);

            if (e instanceof ContentNegotiationException) {
                return ((ContentNegotiationException) e).getSerializedResponse();
            }

            throw new CompletionException(e);
        }

        return response;
    }

    CompletableFuture<Void> processWebSocketClosed(WebSocketRawClient wsClient) {
        // Check for internal issues within the RestJ library. Should never happen.
        if (getReceiverType() != ReceiverType.WEB_SOCKET) throw new IllegalStateException();
        if (getWebSocketEventType() != WebSocketEventType.CLOSED) throw new IllegalStateException();

        return prepareInvocation(null, wsClient)
                .thenCompose(this::invokeHandlerMethod)
                .thenApply(returnValue -> {
                    if (returnValue != null && !returnValue.getClass().getName().equals("kotlin.Unit")) throw new IllegalStateException("WebSocketReceiver method returned some non-null value: " + returnValue);
                    return null;
                });
    }

    CompletableFuture<Void> processWebSocketOpened(HttpRequest request, WebSocketRawClient wsClient) {
        // Check for internal issues within the RestJ library. Should never happen.
        if (getReceiverType() != ReceiverType.WEB_SOCKET) throw new IllegalStateException();
        if (getWebSocketEventType() != WebSocketEventType.OPENED) throw new IllegalStateException();

        return prepareInvocation(request, wsClient)
                .thenCompose(this::invokeHandlerMethod)
                .thenApply(returnValue -> {
                    if (returnValue != null && !returnValue.getClass().getName().equals("kotlin.Unit")) throw new IllegalStateException("WebSocketReceiver method returned some non-null value: " + returnValue);
                    return null;
                });
    }

    CompletableFuture<SerializedHttpResponse> processRequest(HttpRequest request) {
        return prepareInvocation(request, null)
                .thenCompose(this::invokeHandlerMethod)
                .handle(this::mapInvocationResultToResponse)
                .thenApply(response -> getHttpIntegrator().serializeHttpResponse(response, request.getAcceptableResponseTypes()))
                .handle(this::handleContentNegotiationFailure);
    }

    private static class HandlerInvocationContext {
        final Object declaringClassInstance;
        final List<?> methodArguments;

        HandlerInvocationContext(Object declaringClassInstance, List<?> methodArguments) {
            this.declaringClassInstance = declaringClassInstance;
            this.methodArguments = methodArguments;
        }
    }

    private CompletableFuture<HandlerInvocationContext> prepareInvocation(@Nullable HttpRequest request, @Nullable WebSocketRawClient wsClient) {
        // Create an injector which is exclusive to this request.
        // Contains request-specific bindings such as serialized request bodies and http request elements.
        // This injector is used to implement the composite request element functionality.
        final Set<Module> modules = new HashSet<>();
        if (wsClient != null) modules.add(new WebSocketEventModule(wsClient));
        if (request != null) modules.add(new HttpRequestModule(request));
        modules.add(new HttpEndpointModule(getRoute()));
        final var requestInjector = applicationInjector.get()
                .createChildInjector(modules.toArray(Module[]::new));

        final var declaringClassInstance = requestInjector.getInstance(method.getDeclaringClass());

        final var asyncInjectionOrchestrator = requestInjector.getInstance(AsyncInjectionOrchestrator.class);

        return asyncInjectionOrchestrator.getCompletion()
                .thenCompose($ -> asyncInjectionOrchestrator.callInitializers())
                .thenCompose($ -> resolveArguments(declaringClassInstance, requestInjector, request));
    }

    @SuppressWarnings("rawtypes")
    private CompletableFuture<HandlerInvocationContext> resolveArguments(Object declaringClassInstance, Injector requestInjector, HttpRequest request) {
        // Asynchronous deserialization of request elements is supported internally for all request element types (query param,
        // path param, etc.) but only implemented for the request body element, since in practice that is the
        // only potentially massive request element. This list contains the future values of all request elements
        // which are being accepted as a parameter to the handler method.
        List<CompletableFuture> futureArguments = new ArrayList<>();

        for (final var parameter : method.getParameters()) {
            final CompletableFuture argumentFuture;
            if (CustomInjectionPoint.isCustomInjectionPoint(parameter)) {
                argumentFuture = CustomInjectionPoint.of(parameter).resolveValue(request, getHttpIntegrator(), getRoute());
            } else {
                argumentFuture = completedFuture(requestInjector.getInstance(getKey(parameter)));
            }
            futureArguments.add(argumentFuture);
        }

        return CompletableFuture.allOf(futureArguments.toArray(CompletableFuture[]::new))
                .thenApply($ -> futureArguments.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                .thenApply(arguments -> new HandlerInvocationContext(declaringClassInstance, arguments));
    }

    /**
     * Executes this endpoint, potentially asynchronously. If the handler method returns a {@link CompletableFuture}, then
     * the method will be invoked immediately, on the calling thread, and the returned future will be passed through
     * as the return value of this method. If the handler method does not return a {@link CompletableFuture},
     * then the method will be invoked on this thread and the result simply wrapped in a {@link CompletableFuture}.
     *
     * The returned {@link CompletableFuture} contains the deserialized response body, which should be serialized
     * before being sent to the client.
     */
    private CompletableFuture<Object> invokeHandlerMethod(HandlerInvocationContext context) {
        final var isAsyncEndpointMethod = CompletableFuture.class.isAssignableFrom(method.getReturnType());

        method.trySetAccessible();

        // No error wrapping here on purpose. Async endpoints should return exceptional futures instead of
        // throwing exceptions. Any uncaught exceptions WILL crash the server. Perhaps this behavior should
        // be changed if in Stage.PRODUCTION?
        if (isAsyncEndpointMethod) {
            try {
                //noinspection unchecked
                return (CompletableFuture<Object>) method.invoke(context.declaringClassInstance, context.methodArguments.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                return failedFuture(e);
            }
        }

        try {
            return completedFuture(method.invoke(context.declaringClassInstance, context.methodArguments.toArray()));
        } catch (IllegalAccessException e) {
            return failedFuture(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RequestException) {
                return failedFuture(e.getTargetException());
            }
            return failedFuture(e);
        }
    }

    private static Key<?> getKey(Parameter parameter) {
        final var qualifierMarkerTypes = Set.of(Qualifier.class, BindingAnnotation.class);
        final var qualifier = Arrays.stream(parameter.getAnnotations())
                .filter(annotation -> qualifierMarkerTypes.contains(annotation.annotationType()))
                .findFirst();

        if (qualifier.isPresent()) {
            return Key.get(parameter.getParameterizedType(), qualifier.get());
        } else {
            return Key.get(parameter.getParameterizedType());
        }
    }

    ParameterizableRoute getRoute() {
        final var prefix = Optional.ofNullable(method.getDeclaringClass().getAnnotation(HttpResource.class))
                .map(HttpResource::route)
                .map(ParameterizableRoute::parse)
                .orElse(ParameterizableRoute.ROOT);

        final var suffix =  Optional.ofNullable(method.getAnnotation(HttpResource.class))
                .map(HttpResource::route)
                .map(ParameterizableRoute::parse)
                .orElse(ParameterizableRoute.ROOT);

        return prefix.resolve(suffix);
    }

    private HttpIntegrator getHttpIntegrator() {
        return applicationInjector.get().getInstance(HttpIntegrator.class);
    }

    HttpMethod getHttpMethod() {
        if (method.isAnnotationPresent(HttpEndpoint.class) && method.getDeclaringClass().isAnnotationPresent(HttpEndpoint.class))
            throw new IllegalStateException();

        final var annotation = Optional.ofNullable(method.getAnnotation(HttpEndpoint.class))
                .or(() -> Optional.ofNullable(method.getDeclaringClass().getAnnotation(HttpEndpoint.class)))
                .orElseThrow(IllegalStateException::new);

        return annotation.value();
    }

    ReceiverType getReceiverType() {
        if (method.isAnnotationPresent(RequestReceiver.class)) return ReceiverType.REST_REQUEST;
        if (method.isAnnotationPresent(WebSocketEventReceiver.class)) return ReceiverType.WEB_SOCKET;
        return ReceiverType.REST_REQUEST;
    }

    WebSocketEventType getWebSocketEventType() {
        if (!method.isAnnotationPresent(WebSocketEventReceiver.class)) throw new IllegalStateException();
        return method.getAnnotation(WebSocketEventReceiver.class).value();
    }

    private static final Set<Class<? extends Annotation>> markerAnnotations = Set.of(HttpEndpoint.class, RequestReceiver.class,
            WebSocketEventReceiver.class);

    static boolean isHandlerMethod(Method method) {
        return Arrays.stream(method.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .anyMatch(markerAnnotations::contains);
    }
}
