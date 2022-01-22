package com.duncpro.restj;

import com.duncpro.restj.util.ByteArrayPublisher;
import com.duncpro.restj.util.ConsolidatingByteArraySubscriber;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import com.google.inject.Guice;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SmokeTestMain {
    public static void main(String[] args) {
        // RestJ is heavily integrated with Guice.
        // Therefore Guice must be available within the classpath in order to use RestJ.
        // Create a new injector which will hold all declarative HTTP endpoints.
        final var injector = Guice.createInjector(new SmokeTestModule());

        // The HttpRestApi class should be notified of any inbound requests.
        // Given an inbound request it will produce an outbound response using the declarative HTTP endpoints
        // which exist within the Guice injector.
        final var restApi = injector.getInstance(HttpApi.class);

        // Create a request.
        // In practice this process will be handled by whatever http-sever implementation is being used.
        // For example, Netty, Undertow, or Sun HTTP.
        final var request = new HttpRequest(Map.of(), HttpMethod.PUT, new Path("/greeting/duncan"),
                Map.of("Content-Type", List.of("application/json"), "Accept", List.of("text/plain")),
                new ByteArrayPublisher("4".getBytes(StandardCharsets.UTF_8)));


        // This method will route an inbound request to its corresponding handler method, perform deserialization of
        // the request, and finally perform serialization of the response.
        final SerializedHttpResponse response = restApi.processRequest(request).join();

        // Make sure everything worked by printing to console!
        System.out.println(response);
        if (response.getBody().isPresent()) {
            final var bodySubscriber = new ConsolidatingByteArraySubscriber();
            response.getBody().get().subscribe(bodySubscriber);
            System.out.println(new String(bodySubscriber.getFuture().join(), StandardCharsets.UTF_8));
        }
    }
}
