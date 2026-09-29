package com.mustafasen.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// No Kafka broker is running for this smoke test, so the listener container
// is prevented from auto-starting and spinning up connection-retry threads
// against localhost:9092. The real consumer wiring is exercised against a
// live broker in NotificationKafkaIntegrationTest.
// order.events.topic is normally supplied by config-service's config-repo
// (see config-service/src/main/resources/config-repo/notification-service.properties);
// config.import is optional, so without it set here explicitly, the
// @KafkaListener's topic placeholder can't be resolved with no config
// server running in this test.
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "order.events.topic=order-created-events"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
