package com.brandon.qa.tests.mocks;

import com.brandon.qa.client.ProductClient;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.SocketTimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductMockTests {

    // Instance scope gives every concurrent test its own server, stubs and journal.
    @RegisterExtension
    final WireMockExtension mockApi = WireMockExtension.newInstance()
            .options(wireMockConfig().bindAddress("127.0.0.1").dynamicPort())
            .configureStaticDsl(false)
            .failOnUnmatchedRequests(true)
            .build();

    private ProductClient client;

    @BeforeEach
    void setUp() {
        client = new ProductClient(mockApi.baseUrl());
    }

    @Test
    @DisplayName("Product client sends the product ID and accepts JSON")
    void shouldRetrieveStubbedProduct() {
        mockApi.stubFor(get(urlEqualTo("/products/42"))
                // REST Assured's JSON content type accepts several JSON media types.
                .withHeader("Accept", WireMock.containing("application/json"))
                .willReturn(okJson("""
                        {"id":42,"title":"Mock keyboard","price":49.99}
                        """)));

        client.getProductById(42).then()
                .statusCode(200)
                .body("id", equalTo(42))
                .body("title", equalTo("Mock keyboard"))
                .body("price", equalTo(49.99F));

        mockApi.verify(1, getRequestedFor(urlEqualTo("/products/42")));
    }

    @Test
    @DisplayName("Product client sends the requested limit as a query parameter")
    void shouldSendLimitQueryParameter() {
        mockApi.stubFor(get(urlPathEqualTo("/products"))
                .withQueryParam("limit", WireMock.equalTo("2"))
                .willReturn(okJson("""
                        {"products":[{"id":42},{"id":43}],"limit":2,"total":2}
                        """)));

        client.getProducts(2).then()
                .statusCode(200)
                .body("products", hasSize(2))
                .body("limit", equalTo(2));

        mockApi.verify(1, getRequestedFor(urlEqualTo("/products?limit=2")));
    }

    @Test
    @DisplayName("Product client sends the requested category in the path")
    void shouldSendCategoryPath() {
        mockApi.stubFor(get(urlEqualTo("/products/category/smartphones"))
                .willReturn(okJson("""
                        {"products":[{"id":42,"category":"smartphones"}]}
                        """)));

        client.getProductsByCategory("smartphones").then()
                .statusCode(200)
                .body("products", hasSize(1))
                .body("products[0].category", equalTo("smartphones"));

        mockApi.verify(1, getRequestedFor(urlEqualTo("/products/category/smartphones")));
    }

    @Test
    @DisplayName("A deliberate 404 preserves the service error message")
    void shouldExposeNotFoundResponse() {
        // An explicit stub distinguishes this response from an unmatched WireMock request.
        mockApi.stubFor(get(urlEqualTo("/products/9999"))
                .willReturn(aResponse().withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message":"Product not found"}
                                """)));

        client.getProductById(9999).then()
                .statusCode(404)
                .body("message", equalTo("Product not found"));

        mockApi.verify(1, getRequestedFor(urlEqualTo("/products/9999")));
    }

    @Test
    @DisplayName("A 503 response preserves the error body and Retry-After header")
    void shouldExposeServiceUnavailableResponse() {
        mockApi.stubFor(get(urlEqualTo("/products/42"))
                .willReturn(aResponse().withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Retry-After", "3")
                        .withBody("""
                                {"message":"Service temporarily unavailable"}
                                """)));

        client.getProductById(42).then()
                .statusCode(503)
                .header("Retry-After", equalTo("3"))
                .body("message", equalTo("Service temporarily unavailable"));

        // The client exposes HTTP errors to the test; it does not retry them.
        mockApi.verify(1, getRequestedFor(urlEqualTo("/products/42")));
    }

    @Test
    @DisplayName("A delayed response within the read timeout still succeeds")
    void shouldAcceptDelayedResponse() {
        mockApi.stubFor(get(urlEqualTo("/products/42"))
                .willReturn(okJson("{\"id\":42}").withFixedDelay(200)));

        client.getProductById(42).then()
                .statusCode(200)
                .body("id", equalTo(42));

        mockApi.verify(1, getRequestedFor(urlEqualTo("/products/42")));
    }

    @Test
    @DisplayName("A response delayed beyond the read timeout raises a transport error")
    void shouldTimeOutWhenResponseIsTooSlow() {
        mockApi.stubFor(get(urlEqualTo("/products/42"))
                .willReturn(okJson("{\"id\":42}").withFixedDelay(3_000)));
        ProductClient impatientClient = new ProductClient(mockApi.baseUrl(), 500);

        // Assert the transport exception, not an invented HTTP status or exact elapsed time.
        assertThrows(SocketTimeoutException.class,
                () -> impatientClient.getProductById(42));
    }
}
