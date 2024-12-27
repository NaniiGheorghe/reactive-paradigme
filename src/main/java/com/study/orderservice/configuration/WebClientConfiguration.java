package com.study.orderservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${product.service.url}")
    private String productServiceUrl;
    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Bean
    public WebClient productWebClient(WebClient.Builder clientBuilder) {
        return clientBuilder.baseUrl(productServiceUrl).build();
    }

    @Bean
    public WebClient orderSearchWebClient(WebClient.Builder clientBuilder) {
        return clientBuilder.baseUrl(orderServiceUrl).build();
    }

}
