package com.study.orderservice.controller;


import com.study.orderservice.controller.util.OrderNotFoundException;
import com.study.orderservice.domain.user.OrderInfo;
import com.study.orderservice.service.OrderAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/orderAggregationService")
@RequiredArgsConstructor
public class OrderAggregationController {

    private final OrderAggregationService orderAggregationService;

    @GetMapping("/orders")
    public Flux<OrderInfo> getAggregatedOrders(@RequestParam("userId") String userId){
       return orderAggregationService.getAggregatedOrders(userId);
    }

}

