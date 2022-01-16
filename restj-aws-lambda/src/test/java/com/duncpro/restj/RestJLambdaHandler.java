package com.duncpro.restj;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.duncpro.jrest.HttpRestApiModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static java.util.Objects.requireNonNull;

public abstract class RestJLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayRequest, Context context) {
        final var injector = Guice.createInjector(new HttpRestApiModule());
        return injector.getInstance(ApiGatewayHttpRestApi.class)
                .processRequest(apiGatewayRequest);
    }

}
