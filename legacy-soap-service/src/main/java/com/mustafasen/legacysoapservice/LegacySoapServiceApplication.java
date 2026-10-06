package com.mustafasen.legacysoapservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Stands in for an old warehouse system that only speaks SOAP/XML.
@SpringBootApplication
public class LegacySoapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacySoapServiceApplication.class, args);
    }

}
