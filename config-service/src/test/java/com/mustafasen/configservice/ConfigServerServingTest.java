package com.mustafasen.configservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies the native-profile config repo (config-repo/*.properties, bundled
// in this repo) is actually served over HTTP in the flat .properties format
// that spring.config.import=configserver:... clients consume.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerServingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void servesOrderServiceProperties() {
        String body = restTemplate.getForObject("/order-service-default.properties", String.class);

        assertThat(body).contains("inventory.service.name: inventory-service");
        assertThat(body).contains("order.events.topic: order-created-events");
    }

    @Test
    void servesNotificationServiceProperties() {
        String body = restTemplate.getForObject("/notification-service-default.properties", String.class);

        assertThat(body).contains("order.events.topic: order-created-events");
    }

    @Test
    void servesInventoryServiceProperties() {
        String body = restTemplate.getForObject("/inventory-service-default.properties", String.class);

        assertThat(body).contains("inventory.default-low-stock-threshold: 5");
    }
}
