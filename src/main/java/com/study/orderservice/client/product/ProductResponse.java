package com.study.orderservice.client.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ProductResponse {
    private String productId;
    private String productCode;
    private String productName;
    private Integer score;
}
