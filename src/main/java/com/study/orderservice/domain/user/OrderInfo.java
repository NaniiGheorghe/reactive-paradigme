package com.study.orderservice.domain.user;

import lombok.Builder;

@Builder
public record OrderInfo(String orderNumber,
                        String userName,
                        String phoneNumber,
                        String productCode,
                        String productName,
                        String productId,
                        Integer score){
}
