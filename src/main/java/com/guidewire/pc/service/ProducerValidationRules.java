package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;
import com.guidewire.pc.model.ProducerCode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ProducerValidationRules {
    private static final Logger LOGGER = Logger.getLogger(ProducerValidationRules.class.getName());


    public static List<String> validateProducerForPolicy(PolicyPeriod period, ProducerCode producer) {
        LOGGER.log(Level.FINE, "→ ProducerValidationRules.validateProducerForPolicy");
        List<String> errors = new ArrayList<>();
        if (period == null || producer == null) return errors;

        if (!"Active".equalsIgnoreCase(producer.getProducerStatus())) {
            errors.add("PRODUCER_INACTIVE: Producer code " + producer.getCode() + " status is " + producer.getProducerStatus() + " (Policy quotation/binding blocked)");
        }

        String state = period.getBaseState() != null ? period.getBaseState() : "";
        String licensed = producer.getLicensedStates() != null ? producer.getLicensedStates() : "";

        if (state.length() > 0 && !licensed.contains(state)) {
            errors.add("PRODUCER_NOT_LICENSED_IN_STATE: Producer " + producer.getCode() + " is not licensed in policy state " + state);
        }

        return errors;
    }
}
