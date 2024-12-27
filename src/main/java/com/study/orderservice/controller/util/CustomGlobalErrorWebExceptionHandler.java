package com.study.orderservice.controller.util;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

public class CustomGlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private static final String SERVICE_TEMPORARILY_OVERLOADED = "502 Service Temporarily Overloaded";

    public CustomGlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                WebProperties.Resources resources,
                                                ApplicationContext applicationContext) {
        super(errorAttributes, resources, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
                ErrorAttributeOptions.defaults());

        Throwable throwable = getError(request);
        if (throwable instanceof WebClientResponseException.NotFound
            || throwable instanceof WebClientRequestException) {
            return handleBadGatewayException(throwable.getMessage());
        } else {
            return handleGenericError(errorPropertiesMap);
        }
    }

    private Mono<ServerResponse> handleBadGatewayException(String message) {
        ErrorResponse response = new ErrorResponse(SERVICE_TEMPORARILY_OVERLOADED, message);
        return ServerResponse.status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(response));
    }

    private Mono<ServerResponse> handleGenericError(Map<String, Object> errorPropertiesMap) {
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
