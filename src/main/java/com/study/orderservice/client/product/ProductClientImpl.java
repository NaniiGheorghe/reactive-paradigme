package com.study.orderservice.client.product;

import com.study.orderservice.controller.util.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.study.orderservice.util.LogUtil.logOnError;
import static com.study.orderservice.util.LogUtil.logOnNext;

@Slf4j
@Component
@RequiredArgsConstructor
class ProductClientImpl implements ProductClient {

    public static final String PRODUCT_INFO_SERVICE_PRODUCT_NAMES = "/productInfoService/product/names";
    public static final String PARAM_PRODUCT_CODE = "productCode";
    private final WebClient productWebClient;

    @Override
    public Flux<ProductResponse> searchProductByProductCode(String productCode) {
        return productWebClient.get().uri(uriBuilder -> uriBuilder
                        .path(PRODUCT_INFO_SERVICE_PRODUCT_NAMES)
                        .queryParam(PARAM_PRODUCT_CODE, productCode)
                        .build())
                .retrieve()
                .bodyToFlux(ProductResponse.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> Mono.just(new ProductResponse()))
                .doOnEach(logOnNext((e -> log.info("Received product [{}]", e.getProductId()))))
                .doOnEach(logOnError((e -> log.error("Failed getting product. Error message [{}]", e.getMessage()))));
    }

}
