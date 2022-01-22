package com.duncpro.restj;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.duncpro.restj.util.ByteArrayPublisher;
import com.duncpro.restj.util.ConsolidatingByteArraySubscriber;
import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;

import javax.inject.Inject;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.Flow;

import static java.util.Objects.requireNonNull;

public class ApiGatewayHttpRestApi {
    private final HttpApi restApi;

    @Inject
    public ApiGatewayHttpRestApi(HttpApi restApi) {
        this.restApi = requireNonNull(restApi);
    }

    private static Optional<byte[]> readRequestBody(APIGatewayProxyRequestEvent apiGatewayRequest) {
        if (apiGatewayRequest.getBody() == null) return Optional.empty();
        if (apiGatewayRequest.getIsBase64Encoded()) {
            return Optional.of(Base64.getDecoder().decode(apiGatewayRequest.getBody()));
        } else {
            return Optional.of(apiGatewayRequest.getBody().getBytes());
        }
    }

    public APIGatewayProxyResponseEvent processRequest(APIGatewayProxyRequestEvent apiGatewayRequest) {
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
}
