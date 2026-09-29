package com.mustafasen.notificationservice.listeners;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mustafasen.notificationservice.events.OrderCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEventListenerTest {

    private final OrderEventListener listener = new OrderEventListener();
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void attachLogAppender() {
        logAppender = new ListAppender<>();
        logAppender.start();
        listenerLogger().addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        listenerLogger().detachAppender(logAppender);
    }

    private Logger listenerLogger() {
        return (Logger) LoggerFactory.getLogger(OrderEventListener.class);
    }

    @Test
    void handleOrderCreated_logsOrderDetails() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("order-1");
        event.setProductId("product-1");
        event.setQuantity(2);
        event.setStatus("CREATED");

        listener.handleOrderCreated(event);

        assertThat(logAppender.list).hasSize(1);
        String message = logAppender.list.get(0).getFormattedMessage();
        assertThat(message).contains("order-1", "product-1", "2", "CREATED");
    }
}
