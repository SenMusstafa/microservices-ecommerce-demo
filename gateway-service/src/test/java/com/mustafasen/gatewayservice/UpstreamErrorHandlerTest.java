package com.mustafasen.gatewayservice;

import com.mustafasen.gatewayservice.errors.UpstreamErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpstreamErrorHandlerTest {

    private final UpstreamErrorHandler handler = new UpstreamErrorHandler();

    @Test
    void connectionRefused_becomes503WithJsonBody() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/orders"));

        handler.handle(exchange, new RuntimeException("wrapped", new ConnectException("refused"))).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("Service unavailable");
    }

    @Test
    void unrelatedError_isPassedOnToDefaultHandlers() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        IllegalStateException boom = new IllegalStateException("boom");

        assertThatThrownBy(() -> handler.handle(exchange, boom).block()).isSameAs(boom);
    }
}
