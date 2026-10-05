package com.mustafasen.orderservice.errors;

import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static Request request() {
        return Request.create(Request.HttpMethod.POST, "http://inventory-service/x",
                Collections.emptyMap(), null, null, null);
    }

    @Test
    void unreachableInventory_returns503() {
        RetryableException ex = new RetryableException(-1, "Connection refused", Request.HttpMethod.POST,
                new Date(), request());

        var response = handler.handleUnreachable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    void noInstanceRegistered_returns503() {
        FeignException.ServiceUnavailable ex = new FeignException.ServiceUnavailable(
                "no instance", request(), null, Collections.emptyMap());

        assertThat(handler.handleNoInstance(ex).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
