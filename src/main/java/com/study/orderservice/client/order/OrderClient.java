package com.study.orderservice.client.order;

import reactor.core.publisher.Flux;

public interface OrderClient {

    Flux<OrderResponse> searchOrderByPhoneNumber(String phoneNumber);

}
