package com.guidewire.ig.credit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.logging.Logger;
import java.util.logging.Level;

@SpringBootApplication
public class CreditFraudIGApplication {
    private static final Logger LOGGER = Logger.getLogger(CreditFraudIGApplication.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.FINE, "→ CreditFraudIGApplication.main");
        SpringApplication.run(CreditFraudIGApplication.class, args);
    }
}
