package com.mustafasen.inventoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "eureka.client.enabled=false")
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
