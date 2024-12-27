package com.study.orderservice.client.product;

import reactor.core.publisher.Flux;

public interface ProductClient {

    Flux<ProductResponse> searchProductByProductCode(String code);

}
