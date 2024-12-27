package com.study.orderservice.client.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.study.orderservice.util.LogUtil.logOnError;
import static com.study.orderservice.util.LogUtil.logOnNext;

@Slf4j
@Component
@RequiredArgsConstructor
class OrderClientImpl implements OrderClient {

    public static final String ORDER_SEARCH_SERVICE_ORDER_PHONE = "/orderSearchService/order/phone";
    public static final String PARAM_PHONE_NUMBER = "phoneNumber";
    private final WebClient orderSearchWebClient;

    @Override
    public Flux<OrderResponse> searchOrderByPhoneNumber(String phoneNumber) {
        return orderSearchWebClient.get().uri(uriBuilder -> uriBuilder
                        .path(ORDER_SEARCH_SERVICE_ORDER_PHONE)
                        .queryParam(PARAM_PHONE_NUMBER, phoneNumber)
                        .build())
                .retrieve()
                .bodyToFlux(OrderResponse.class)
                .doOnEach(logOnNext((e -> log.info("Received order [{}]", e.orderNumber()))))
                .doOnEach(logOnError((e -> log.error("Failed getting order. Error message [{}]", e.getMessage()))));
    }
}
