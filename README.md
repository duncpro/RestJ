# RestJ
Object-oriented, asynchronous, serverside REST framework, leveraging Java 11, Guice, and CompletableFuture.

Not an HTTP server implementation. An underlying server library, such as Netty, must be used in addition to RestJ.
This library is designed as a lightweight alternative to JAX-RS and its implementations, supporting both async and blocking
applications.

## Example
See the SmokeTest example within `src/test` directory.

## Full Documentation

### Declarative Endpoint Method
```java
@HttpEndpoint(M)
@HttpResource(P)
CompletableFuture<R> f(A);
```
where `M` is some `HttpMethod`.\
where `P` is some route-like `String`, for instance `/pets/{id}`.\
where `A` is zero or more *HTTP Request Elements*.\
where `R` is some serializable response body.

`@HttpResource` is optional, if there is no inheritable resource route then `Route.ROOT` is implied.
If an `@HttpResource` annotation exists on the declaring class then the endpoint will exist at the
concatenation of the two routes.

Endpoint methods may return either `CompletableFuture<R>` or `R` directly. 
Operations performed within the endpoint method will block the thread which
invoked `HttpRestApi#processRequest`.
#### Example
```java
@HttpResource(route = "/users/{userName}")
class PetStore {
    @HttpEndpoint(HttpMethod.GET)
    @HttpResource("/pet/{petId}")
    CompletableFuture<Pet> handleGetPetRequest(@Path("petId") UUID petId);
}
```

### HTTP Request Element
Some (potentially annotated) type for which an instance can be derived given an `HttpRequest`  and
the application's general context. Request Elements can be either composite, or atomic. Composite elements consist
of zero or more atomic elements.
#### Exhaustive Set of Atomic Request Elements
- @Path *IntegratableType*
- @Query *IntegratableType*
- @Header *IntegratableType*
- @Body *IntegratableType*
- @Inject `HttpRequest`

#### Defining Composite Request Elements
A composite element is defined as some class, which can be constructed by Guice, and contains zero or
more injectable fields corresponding to dependency request elements, atomic or composite.

For instance, the class `PetIdentity` is a composite request element which could be derived from an endpoint
positioned at the route `/users/{user}/pets/{petName}`.
```java
class PetIdentity {
    @Path("user")
    private String owner;
    
    @Path("petName")
    private String petName;
}
```


This composite request element may be consumed by other composite request elements which declare it as a field annotated with
`@Inject`. Additionally, it may be consumed directly by any request handler method which declares it as a parameter. For instance...
```java
@HttpEndpoint(POST)
CompletableFuture<Void> handlePostRequest(PetIdentity pet);
```

There is no need to register types which represent composite request elements with Guice. Instead, Guice
will construct them just-in-time.

#### Optional Atomic Request Elements
Unlike composite request elements, atomic request elements can be optional.
This is accomplished by marking the injection point (method parameter or injected field) as `@Nullable`.

For example, a service might provide two ways to authenticate, and abstract these behind a single `AuthenticatedUser` element.

```java
class AuthenticatedUser {
    @Header("authToken")
    @Nullable
    private String header;
    
    @Query("authToken")
    @Nullable
    private String queryArg;
    
    public String getAuthToken() {
        if (header != null) return header;
        if (queryArg != null) return queryArg;
        throw new BadRequestException();
    }
}
```

### Shared Request Elements
In many cases, there are request elements that are shared between all endpoints within a resource. 
These shared elements can be declared outside-of the handler method parameter list, and within the
declaring class as a field. This can make your code more concise by shortening the length
of request handler method declarations.

For instance, take some `AuthenticatedUser` request element, and assume this element
is required for invocation of every endpoint within some resource. We could declare
the element as a field in the enclosing resource class like so....
```java
@HttpResource(route = "/todos")
class MyResource {
    // Application Services
    @Inject
    private TodoDatabase database;
    
    // Common Request Elements
    @Inject
    private AuthenticatedUser user;
    
    @HttpEndpoint(POST)
    CompletableFuture<UUID> handleAddTodoRequest(@RequestBody String todo) {
        return database.addTodo(user, todo);
    }
    
    @HttpResource("/{id}")
    @HttpEndpoint(DELETE)
    CompletableFuture<Void> handleDeleteTodoRequest(@Path("id") UUID todoId) {
        return database.removeTodo(user, todoId);
    }
}
```

### Async Injection
Unlike standard `@Inject` injections which are performed by Guice, atomic
request element injections (`@Path`, `@Query`, `@RequestBody`, etc.) are performed asynchronously. This means
that standard `@PostConstruct` methods might be executed before injection is complete. To remedy this, an optionally
implementable interface `CompositeRequestElement` is provided. 
This interface has a method `init` which is guaranteed to be called only after all async injections have completed.

The HTTP handler method associated with an endpoint will only be called after all async injections have been performed
and all `init` methods have been called. 

`init` methods are called in the same order in which Guice provisions the request elements.

A side effect of the async injection mechanism is that `Providers` for composite
request elements can not be injected. Attempting to inject a provider for one of these
types will result in an `UnsupportedOperationException` when calling `Provider#get`. In practice, there should never be
the need to inject a provider for a composite request element.

### Exceptions
Any exception thrown within an async handler method (that is, a handler method which returns `CompletableFuture`)
will not be caught. Async handler methods should return failing `CompletableFuture` instead. 


Any exception thrown within a sync handler method (that is, a handler method which does not return `CompletableFuture`),
will not be caught unless it is an instance of `RequestException` in which case it will be used 
to generate an exception HTTP response.

An async handler method which completes with some exception other than `RequestException` will result
in an uncaught `CompletionException` being thrown from `HttpRestApi#processRequest`.

### Web Sockets
Barebones support for Web Sockets is included, however the API is incomplete and will likely be expanded in the future.
Only the `restj-undertow` implementation supports Web Sockets.

Sending messages to web socket clients is supported, but receiving data is not. Any received data is ignored.
Consider having the client make a standard HTTP request instead.

```java
import java.net.http.HttpRequest;
import java.time.ZonedDateTime;

@HttpEndpoint(HttpMethod.GET)
@HttpResource(route = "/weather")
class WeatherEventsApi {
    // A @RequestReceiver method handles standard HTTP requests made to the endpoint.
    // That is, requests which do not support the web socket protocol.
    // Therefore, servers can implement support for older clients quite easily alongside
    // more modern web socket clients. The opposite of a @RequestReceiver is a @WebSocketEventReceiver.
    @RequestReceiver
    CompletableFuture<Void> handlePollingClients(@Query("since") ZonedDateTime lastPoll) {
        return failedFuture(new BadRequestException("Weather event polling is not yet implemented."));
    }

    @WebSocketEventReceiver(WebSocketEventType.OPENED)
    void onSubscribe(WebSocketRawClient client, @Header("Authorization") String authToken) {
        // TODO Start sending this client weather events
    }

    @WebSocketEventReceiver(WebSocketEventType.CLOSED)
    void onUnsubscribe(WebSocketRawClient client) {
        // TODO Stop sending this client weather events
    }
}
```

### Choosing an Implementation
For smaller scale applications which rely on blocking technologies such as JDBC, `restj-sun-http-server`
can be used. It uses Sun's HTTP server library which is included within most JDK distributions.
Alternatively, `restj-undertow` can be used alongside a blocking worker thread pool.

For larger scale applications with thousands of simultaneous requests using an async implementation 
like `restj-undertow` might be preferred. 

An implementation for AWS Lambda with API Gateway Proxy integration is also provided in `restj-aws-lambda`.
Therefore, an application developed using RestJ can be tested completely locally, and then deployed as a serverless
service on AWS.
