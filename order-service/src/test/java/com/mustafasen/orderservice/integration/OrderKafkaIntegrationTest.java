package com.mustafasen.orderservice.integration;

import com.mustafasen.orderservice.clients.InventoryClient;
import com.mustafasen.orderservice.dtos.CreateOrderRequest;
import com.mustafasen.orderservice.dtos.OrderResponse;
import com.mustafasen.orderservice.dtos.ReservationResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Closes the gap noted at the end of Phase 2: the Kafka publish path could
// not be live-verified in that sandbox because the Docker network was
// corrupting inter-container TCP (order-service/notification-service <->
// Kafka/Zookeeper). This test uses Testcontainers in single-node KRaft mode
// (no separate Zookeeper container, so no inter-container hop) to boot a
// real broker and prove OrderEventPublisher actually publishes a readable
// OrderCreatedEvent to it.
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "order.events.topic=order-created-events"
})
class OrderKafkaIntegrationTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0")).withKraft();

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private InventoryClient inventoryClient;

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUpConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-integration-test");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("order-created-events"));
    }

    @AfterEach
    void tearDownConsumer() {
        consumer.close();
    }

    @Test
    void placingOrder_publishesOrderCreatedEvent_toRealKafkaBroker() {
        ReservationResult reservation = new ReservationResult();
        reservation.setSuccess(true);
        reservation.setMessage("Reserved");
        reservation.setRemainingQuantity(49);
        when(inventoryClient.reserve(anyString(), anyInt())).thenReturn(reservation);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId("product-1");
        request.setQuantity(1);

        OrderResponse response = restTemplate.postForObject("/api/v1/orders", request, OrderResponse.class);
        assertThat(response.getStatus()).isEqualTo("CREATED");

        List<ConsumerRecord<String, String>> records = pollUntilNotEmpty(Duration.ofSeconds(15));

        assertThat(records).anySatisfy(record -> {
            assertThat(record.key()).isEqualTo(response.getOrderId());
            assertThat(record.value()).contains("\"productId\":\"product-1\"");
            assertThat(record.value()).contains("\"status\":\"CREATED\"");
        });
    }

    private List<ConsumerRecord<String, String>> pollUntilNotEmpty(Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        while (System.currentTimeMillis() < deadline && result.isEmpty()) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            records.forEach(result::add);
        }
        return result;
    }
}
