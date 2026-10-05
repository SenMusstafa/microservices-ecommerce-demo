package com.mustafasen.inventoryservice;

import com.mustafasen.inventoryservice.services.InventoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final InventoryService inventoryService;

    public DataSeeder(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public void run(String... args) {
        inventoryService.seedData();
    }
}
