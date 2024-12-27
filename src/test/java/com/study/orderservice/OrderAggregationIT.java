package com.study.orderservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.study.orderservice.client.order.OrderResponse;
import com.study.orderservice.client.product.ProductResponse;
import com.study.orderservice.config.DefaultTestConfiguration;
import com.study.orderservice.controller.util.ErrorResponse;
import com.study.orderservice.domain.user.OrderInfo;
import com.study.orderservice.repository.user.UserEntity;
import com.study.orderservice.repository.user.UserInfoRepository;
import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.study.orderservice.util.TestUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@WebAppConfiguration
@ContextConfiguration(classes = DefaultTestConfiguration.class)
@AutoConfigureWebTestClient
public class OrderAggregationIT {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0").withExposedPorts(27017);
    private static final WireMockServer orderWireMockServer = new WireMockServer(8081);
    private static final WireMockServer productWireMockServer = new WireMockServer(8082);
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserInfoRepository userInfoRepository;

    @BeforeAll
    public static void setUpAll() {
        mongoDBContainer.start();
        orderWireMockServer.start();
        productWireMockServer.start();
    }

    @BeforeEach
    public void setUp() {
        ingestUserData();
    }

    @BeforeEach
    public void tearDown() {
        deleteUserData();
    }

    @AfterAll
    static void tearDownAll() {
        mongoDBContainer.stop();
        orderWireMockServer.stop();
        productWireMockServer.stop();
    }

    @Test
    public void testGetAggregatedOrdersWhenOrderAndProductIsPresent_orderInfoIsPresent() throws Exception {
        mockOrderSearchService();
        mockProductInfoService();

        List<OrderInfo> orderInfos = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orderAggregationService/orders")
                        .queryParam(USER_ID_QUERY_PARAM_NAME, USER_ID)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<List<OrderInfo>>() {})
                .getResponseBody()
                .blockFirst();

        assert orderInfos != null;
        OrderInfo orderInfo = orderInfos.getFirst();
        assertThat(orderInfo.orderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(orderInfo.productCode()).isEqualTo(PRODUCT_CODE);
        assertThat(orderInfo.phoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(orderInfo.productId()).isEqualTo(PRODUCT_ID);
        assertThat(orderInfo.productName()).isEqualTo(PRODUCT_NAME);
        assertThat(orderInfo.score()).isEqualTo(PRODUCT_SCORE);
    }

    @Test
    public void testGetAggregatedOrdersWhenNoOrderFound_emptyFlux() {
        ErrorResponse response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orderAggregationService/orders")
                        .queryParam(USER_ID_QUERY_PARAM_NAME, USER_ID)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(502)
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();

        assertThat(response.message()).isEqualTo("502 Service Temporarily Overloaded");
        assertThat(response.details()).isEqualTo("404 Not Found from GET http://localhost:8081/orderSearchService/order/phone");
    }

    @Test
    public void testGetAggregatedOrdersWhenNoProductServiceDown_orderInfoWithoutProduct() throws JsonProcessingException {
        mockOrderSearchService();

        List<OrderInfo> orderInfos = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orderAggregationService/orders")
                        .queryParam(USER_ID_QUERY_PARAM_NAME, USER_ID)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<List<OrderInfo>>() {})
                .getResponseBody()
                .blockFirst();

        assert orderInfos != null;
        OrderInfo orderInfo = orderInfos.getFirst();
        assertThat(orderInfo.orderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(orderInfo.productCode()).isEqualTo(PRODUCT_CODE);
        assertThat(orderInfo.phoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(orderInfo.productId()).isNull();
        assertThat(orderInfo.productName()).isNull();
        assertThat(orderInfo.score()).isNull();
    }

    private void mockProductInfoService() throws JsonProcessingException {
        ProductResponse value = new ProductResponse(PRODUCT_ID, PRODUCT_CODE, PRODUCT_NAME, PRODUCT_SCORE);
        productWireMockServer.stubFor(
                WireMock.get(urlPathEqualTo("/productInfoService/product/names"))
                        .withQueryParam(PRODUCT_CODE_QUERY_PARAMETER_NAME, equalTo(PRODUCT_CODE))
                        .willReturn(ok(new ObjectMapper().writeValueAsString(value))
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    private void mockOrderSearchService() throws JsonProcessingException {
        OrderResponse orderResponse = new OrderResponse(PHONE_NUMBER, ORDER_NUMBER, PRODUCT_CODE);
        orderWireMockServer.stubFor(
                WireMock.get(urlPathEqualTo("/orderSearchService/order/phone"))
                        .withQueryParam(PHONE_NUMBER_QUERY_PARAM_NAME, equalTo(PHONE_NUMBER))
                        .willReturn(ok(new ObjectMapper().writeValueAsString(orderResponse))
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    private void ingestUserData() {
        userInfoRepository.save(new UserEntity(USER_ID, USER_NAME, PHONE_NUMBER)).block();
    }

    private void deleteUserData() {
        userInfoRepository.deleteAll().block();
    }

}
