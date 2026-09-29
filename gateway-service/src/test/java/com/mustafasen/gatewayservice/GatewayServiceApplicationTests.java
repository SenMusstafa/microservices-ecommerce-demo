package com.mustafasen.gatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "eureka.client.enabled=false")
class GatewayServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
