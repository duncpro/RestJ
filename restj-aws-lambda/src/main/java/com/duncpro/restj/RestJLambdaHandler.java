package com.duncpro.restj;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.duncpro.jrest.HttpRequest;
import com.duncpro.jrest.HttpRestApi;
import com.duncpro.jrest.HttpRestApiModule;
import com.duncpro.jrest.util.ByteArrayPublisher;
import com.duncpro.jrest.util.ConsolidatingByteArraySubscriber;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import com.google.inject.Injector;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public abstract class RestJLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private Optional<byte[]> readRequestBody(APIGatewayProxyRequestEvent apiGatewayRequest) {
        if (apiGatewayRequest.getBody() == null) return Optional.empty();
        if (apiGatewayRequest.getIsBase64Encoded()) {
            return Optional.of(Base64.getDecoder().decode(apiGatewayRequest.getBody()));
        } else {
            return Optional.of(apiGatewayRequest.getBody().getBytes());
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
        final var injector = createApplicationInjector().createChildInjector(new HttpRestApiModule());
        final var restApi = injector.getInstance(HttpRestApi.class);

        Optional<Flow.Publisher<byte[]>> requestBodyPublisher = readRequestBody(apiGatewayRequest)
                .map(ByteArrayPublisher::new);

        final var restjRequest = new HttpRequest(
                apiGatewayRequest.getMultiValueQueryStringParameters(),
                HttpMethod.valueOf(apiGatewayRequest.getHttpMethod()),
                new Path(apiGatewayRequest.getPath()),
                apiGatewayRequest.getMultiValueHeaders(),
                requestBodyPublisher.orElse(null)
        );

        final var restjResponse = restApi.processRequest(restjRequest).join();

        final var apiGatewayResponse = new APIGatewayProxyResponseEvent();
        apiGatewayResponse.setMultiValueHeaders(restjResponse.getHeader());
        apiGatewayResponse.setStatusCode(restjResponse.getStatusCode());
        if (restjResponse.getBody().isPresent()) {
            final var bodySubscriber = new ConsolidatingByteArraySubscriber();
            restjResponse.getBody().get().subscribe(bodySubscriber);
            final var responseBody = Base64.getEncoder().encodeToString(bodySubscriber.getFuture().join());
            apiGatewayResponse.setIsBase64Encoded(true);
            apiGatewayResponse.setBody(responseBody);
        }

        return apiGatewayResponse;
    }

    public abstract Injector createApplicationInjector();
}
