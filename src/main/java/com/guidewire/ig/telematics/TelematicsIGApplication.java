package com.guidewire.ig.telematics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.logging.Logger;
import java.util.logging.Level;

@SpringBootApplication
public class TelematicsIGApplication {
    private static final Logger LOGGER = Logger.getLogger(TelematicsIGApplication.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.FINE, "→ TelematicsIGApplication.main");
        SpringApplication.run(TelematicsIGApplication.class, args);
    }
}
