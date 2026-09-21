package com.mustafasen.notificationservice.listeners;

import com.mustafasen.notificationservice.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @KafkaListener(topics = "${order.events.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Notification: order {} for product {} (qty {}) is {}",
                event.getOrderId(), event.getProductId(), event.getQuantity(), event.getStatus());
    }
}
