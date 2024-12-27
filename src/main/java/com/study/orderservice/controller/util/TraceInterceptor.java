package com.study.orderservice.controller.util;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.study.orderservice.util.LogUtil.MDC_TRACE_ID;

@Component
public class TraceInterceptor implements WebFilter {

    public static final String REQUEST_ID = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        List<String> headers = exchange.getRequest().getHeaders().get(REQUEST_ID);
        String traceId = Optional.ofNullable(headers)
                .orElse(Collections.emptyList())
                .stream()
                .findFirst()
                .orElse(UUID.randomUUID().toString());
        MDC.put(MDC_TRACE_ID, traceId);
        return chain.filter(exchange)
                .doFinally(signalType -> MDC.remove(MDC_TRACE_ID))
                .contextWrite(Context.of(MDC_TRACE_ID, traceId));
    }

}
