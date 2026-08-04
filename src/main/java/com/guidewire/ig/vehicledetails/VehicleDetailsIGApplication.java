package com.guidewire.ig.vehicledetails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.logging.Logger;
import java.util.logging.Level;

@SpringBootApplication
public class VehicleDetailsIGApplication {
    private static final Logger LOGGER = Logger.getLogger(VehicleDetailsIGApplication.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.FINE, "→ VehicleDetailsIGApplication.main");
        SpringApplication.run(VehicleDetailsIGApplication.class, args);
    }
}
