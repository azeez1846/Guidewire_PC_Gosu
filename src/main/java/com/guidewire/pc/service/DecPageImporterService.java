package com.guidewire.pc.service;

import com.guidewire.pc.constants.PCConstants;
import com.guidewire.pc.model.Account;
import com.guidewire.pc.model.PolicyPeriod;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecPageImporterService {
    private static final Logger LOGGER = Logger.getLogger(DecPageImporterService.class.getName());
    private static final DecPageImporterService instance = new DecPageImporterService();

    private DecPageImporterService() {
        LOGGER.log(Level.FINE, "→ DecPageImporterService.DecPageImporterService");}

    public static DecPageImporterService getInstance() {
        LOGGER.log(Level.FINE, "→ DecPageImporterService.getInstance");
        return instance;
    }

    /**
     * Parses raw dec page text/JSON and converts into a populated PolicyPeriod draft
     */
    public PolicyPeriod importDecPageText(String rawDecPageText) {
        LOGGER.log(Level.FINE, "→ DecPageImporterService.importDecPageText");
        if (rawDecPageText == null || rawDecPageText.trim().isEmpty()) {
            throw new IllegalArgumentException("Dec page text cannot be empty");
        }

        PolicyPeriod period = new PolicyPeriod();
        period.setStatus(PCConstants.STATUS_DRAFT);
        period.setJobType(PCConstants.JOB_TYPE_SUBMISSION);

        // Parse Product Line
        if (rawDecPageText.contains("Personal Auto") || rawDecPageText.contains("Auto Policy")) {
            period.setProductCode(PCConstants.PRODUCT_PERSONAL_AUTO);
        } else if (rawDecPageText.contains("Property") || rawDecPageText.contains("Commercial Building")) {
            period.setProductCode(PCConstants.PRODUCT_COMMERCIAL_PROPERTY);
        } else if (rawDecPageText.contains("General Liability")) {
            period.setProductCode(PCConstants.PRODUCT_GENERAL_LIABILITY);
        } else {
            period.setProductCode(PCConstants.PRODUCT_COMMERCIAL_AUTO);
        }

        // Parse Account / Named Insured using regex
        String insuredName = extractRegex(rawDecPageText, "Named Insured:\\s*([^\\n,]+)", "Imported Account");
        Account acc = new Account();
        acc.setAccountHolderName(insuredName);
        acc.setAccountNumber("A-IMP-" + (System.currentTimeMillis() % 89999 + 10000));
        DataStoreService.getInstance().createAccount(acc);
        period.setAccount(acc);

        // Parse Limits & Deductibles
        String biLimit = extractRegex(rawDecPageText, "Bodily Injury:\\s*([^\\n]+)", "$500k/$500k");
        String pdLimit = extractRegex(rawDecPageText, "Property Damage:\\s*([^\\n]+)", "$250k");
        String collDeduct = extractRegex(rawDecPageText, "Collision Deductible:\\s*([^\\n]+)", "$1000");

        period.setBodilyInjuryLimit(biLimit);
        period.setPropertyDamageLimit(pdLimit);
        period.setCollisionDeductible(collDeduct);

        // Rate the imported draft
        RatingEngine.getInstance().rate(period);
        DataStoreService.getInstance().createSubmission(period);
        LOGGER.log(Level.INFO, "Successfully imported Dec Page for insured: {0} Job Number: {1}", new Object[]{insuredName, period.getJobNumber()});

        return period;
    }

    private String extractRegex(String text, String regex, String defaultVal) {
        LOGGER.log(Level.FINE, "→ DecPageImporterService.extractRegex");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return defaultVal;
    }
}
