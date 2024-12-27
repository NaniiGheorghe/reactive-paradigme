package com.study.orderservice.configuration;

import com.study.orderservice.controller.util.CustomGlobalErrorWebExceptionHandler;
import com.study.orderservice.controller.util.OrderNotFoundException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.ViewResolver;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient productWebClient(WebClient.Builder clientBuilder) {
        //TODO use env variable
        return clientBuilder.baseUrl("http://localhost:8082").build();
    }

    @Bean
    public WebClient orderSearchWebClient(WebClient.Builder clientBuilder) {
        //TODO use env variable
        return clientBuilder.baseUrl("http://localhost:8081").build();
    }

    @ExceptionHandler({OrderNotFoundException.class})
    protected ResponseEntity<String> handleCustomError(RuntimeException ex) {
        return ResponseEntity.status(400).body("TESADASDASDT");
    }


    @Bean
    @ConditionalOnMissingBean(value = {ErrorWebExceptionHandler.class}, search = SearchStrategy.CURRENT)
    @Order(-1)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             WebProperties webProperties,
                                                             ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer,
                                                             ApplicationContext applicationContext) {
        AbstractErrorWebExceptionHandler exceptionHandler = new CustomGlobalErrorWebExceptionHandler(
                errorAttributes, webProperties.getResources(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

}
