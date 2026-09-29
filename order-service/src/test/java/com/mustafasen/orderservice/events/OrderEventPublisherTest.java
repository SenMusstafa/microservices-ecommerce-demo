package com.mustafasen.orderservice.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Test
    void publishOrderCreated_sendsEventKeyedByOrderId_toConfiguredTopic() {
        OrderEventPublisher publisher = new OrderEventPublisher(kafkaTemplate, "order-created-events");
        OrderCreatedEvent event = new OrderCreatedEvent("order-1", "product-1", 2, "CREATED");

        publisher.publishOrderCreated(event);

        verify(kafkaTemplate).send("order-created-events", "order-1", event);
        verifyNoMoreInteractions(kafkaTemplate);
    }
}
