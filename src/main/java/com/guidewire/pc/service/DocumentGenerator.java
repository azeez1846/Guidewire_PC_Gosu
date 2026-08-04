package com.guidewire.pc.service;

import com.guidewire.pc.model.PolicyPeriod;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentGenerator {
    private static final Logger LOGGER = Logger.getLogger(DocumentGenerator.class.getName());
    private static final DocumentGenerator instance = new DocumentGenerator();

    private DocumentGenerator() {
        LOGGER.log(Level.FINE, "→ DocumentGenerator.DocumentGenerator");}

    public static DocumentGenerator getInstance() {
        LOGGER.log(Level.FINE, "→ DocumentGenerator.getInstance");
        return instance;
    }

    public byte[] generatePolicyBinderPdf(PolicyPeriod period) throws IOException {
        LOGGER.log(Level.FINE, "→ DocumentGenerator.generatePolicyBinderPdf");
        LOGGER.log(Level.INFO, "Generating Policy Binder PDF document for job: {0}", period.getJobNumber());

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // Header Title
                cs.beginText();
                cs.setFont(boldFont, 18);
                cs.newLineAtOffset(50, 740);
                cs.showText("GUIDEWIRE POLICYCENTER - OFFICIAL POLICY BINDER");
                cs.endText();

                // Subheader / Divider line
                cs.beginText();
                cs.setFont(regularFont, 10);
                cs.newLineAtOffset(50, 720);
                cs.showText("Generated Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                cs.endText();

                // Section 1: Policy & Job Details
                int y = 680;
                writeTextLine(cs, boldFont, 14, 50, y, "1. POLICY IDENTIFICATION");
                y -= 25;
                writeTextLine(cs, regularFont, 11, 65, y, "Job Number: " + (period.getJobNumber() != null ? period.getJobNumber() : "N/A"));
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Policy Number: " + (period.getPolicyNumber() != null ? period.getPolicyNumber() : "PENDING ISSUANCE"));
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Product Code: " + period.getProductCode());
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Policy Status: " + period.getFormattedStatus());
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Term Months: " + period.getTermMonths() + " Months");

                // Section 2: Account Holder Info
                y -= 35;
                writeTextLine(cs, boldFont, 14, 50, y, "2. INSURED ACCOUNT DETAILS");
                y -= 25;
                if (period.getAccount() != null) {
                    writeTextLine(cs, regularFont, 11, 65, y, "Account Holder: " + period.getAccount().getAccountHolderName());
                    y -= 18;
                    writeTextLine(cs, regularFont, 11, 65, y, "Account Number: " + period.getAccount().getAccountNumber());
                    y -= 18;
                    writeTextLine(cs, regularFont, 11, 65, y, "Address: " + period.getAccount().getFormattedAddress());
                } else {
                    writeTextLine(cs, regularFont, 11, 65, y, "Account Holder: Unassigned");
                }

                // Section 3: Financials & Coverage Summary
                y -= 35;
                writeTextLine(cs, boldFont, 14, 50, y, "3. COVERAGES & PREMIUM SUMMARY");
                y -= 25;
                writeTextLine(cs, regularFont, 11, 65, y, "Bodily Injury Limit: " + period.getBodilyInjuryLimit());
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Property Damage Limit: " + period.getPropertyDamageLimit());
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Comprehensive Deductible: " + period.getComprehensiveDeductible());
                y -= 18;
                writeTextLine(cs, regularFont, 11, 65, y, "Collision Deductible: " + period.getCollisionDeductible());
                y -= 25;
                writeTextLine(cs, boldFont, 12, 65, y, "Base Premium: $" + period.getBasePremium());
                y -= 18;
                writeTextLine(cs, boldFont, 12, 65, y, "Taxes & Statutory Fees: $" + period.getTaxesAndFees());
                y -= 20;
                writeTextLine(cs, boldFont, 13, 65, y, "TOTAL BOUND PREMIUM: $" + period.getTotalPremium());

                // Footer
                writeTextLine(cs, regularFont, 9, 50, 50, "Guidewire PolicyCenter (Gosu) Standalone Engine - Confidential Document");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void writeTextLine(PDPageContentStream cs, PDType1Font font, int fontSize, int x, int y, String text) throws IOException {
        LOGGER.log(Level.FINE, "→ DocumentGenerator.writeTextLine");
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}
