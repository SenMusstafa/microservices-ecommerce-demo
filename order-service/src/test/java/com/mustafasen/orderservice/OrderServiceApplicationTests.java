package com.mustafasen.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// order.events.topic is normally supplied by config-service's config-repo
// (see config-service/src/main/resources/config-repo/order-service.properties);
// config.import is optional, so without it set here explicitly, bean
// creation fails with no config server running in this test.
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "order.events.topic=order-created-events"
})
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
