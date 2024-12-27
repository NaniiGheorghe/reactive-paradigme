package com.study.orderservice.service;

import com.study.orderservice.client.order.OrderResponse;
import com.study.orderservice.client.order.OrderClient;
import com.study.orderservice.client.product.ProductClient;
import com.study.orderservice.client.product.ProductResponse;
import com.study.orderservice.controller.util.OrderNotFoundException;
import com.study.orderservice.domain.user.OrderInfo;
import com.study.orderservice.domain.user.User;
import com.study.orderservice.repository.user.UserEntityMapper;
import com.study.orderservice.repository.user.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.study.orderservice.util.LogUtil.logOnError;
import static com.study.orderservice.util.LogUtil.logOnNext;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAggregationService {

    private static final UserEntityMapper USER_ENTITY_MAPPER = UserEntityMapper.INSTANCE;
    private final UserInfoRepository userInfoRepository;
    private final OrderClient orderClient;
    private final ProductClient productClient;

    public Flux<OrderInfo> getAggregatedOrders(String userId) {
        Mono<User> user = userInfoRepository.findDistinctFirstById(userId).map(USER_ENTITY_MAPPER::toDomainModel);
        return user.flatMapMany(userEntity -> orderClient.searchOrderByPhoneNumber(userEntity.getPhone())
                 .parallel()
                 .flatMap(orderResponse -> productClient.searchProductByProductCode(orderResponse.productCode())
                         .reduce((product, product2) -> product.getScore() > product2.getScore() ? product : product2)
                         .switchIfEmpty(Mono.just(new ProductResponse()))
                         .map(productResponse -> buildOrderInfo(userEntity, orderResponse, productResponse))
                         .doOnEach(logOnNext((e -> log.info("Aggregated order [{}]", e.orderNumber()))))
                         .doOnEach(logOnError((e -> log.error("Failed to aggregate order. Error message [{}]", e.getMessage()))))
               ));
    }

    private static OrderInfo buildOrderInfo(User userEntity, OrderResponse orderResponse, ProductResponse productResponse) {
        return OrderInfo.builder()
                .orderNumber(orderResponse.orderNumber())
                .userName(userEntity.getName())
                .phoneNumber(userEntity.getPhone())
                .productCode(orderResponse.productCode())
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .score(productResponse.getScore())
                .build();
    }
}
