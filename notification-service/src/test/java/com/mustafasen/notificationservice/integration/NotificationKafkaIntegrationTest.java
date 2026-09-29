package com.mustafasen.notificationservice.integration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mustafasen.notificationservice.listeners.OrderEventListener;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

// Closes the other half of the Phase 2 gap: proves the real @KafkaListener
// bean in this service actually receives and deserializes an event produced
// to a live Kafka broker, using Testcontainers' single-node KRaft mode so no
// inter-container network hop (the thing that broke in the Phase 2 sandbox)
// is involved.
@Testcontainers
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "order.events.topic=order-created-events"
})
class NotificationKafkaIntegrationTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0")).withKraft();

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

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
    void listenerConsumesAndLogs_realOrderCreatedEventFromKafka() throws Exception {
        String orderJson = "{\"orderId\":\"order-42\",\"productId\":\"product-1\",\"quantity\":3,\"status\":\"CREATED\"}";

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>("order-created-events", "order-42", orderJson)).get();
        }

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(logAppender.list)
                        .anySatisfy(event -> assertThat(event.getFormattedMessage())
                                .contains("order-42")
                                .contains("product-1")
                                .contains("CREATED"))
        );
    }
}
