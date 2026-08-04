package com.guidewire.ig.address;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.logging.Logger;
import java.util.logging.Level;

@SpringBootApplication
public class AddressStandardizationIGApplication {
    private static final Logger LOGGER = Logger.getLogger(AddressStandardizationIGApplication.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.FINE, "→ AddressStandardizationIGApplication.main");
        SpringApplication.run(AddressStandardizationIGApplication.class, args);
    }
}
