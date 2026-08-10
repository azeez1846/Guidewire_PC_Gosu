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

/**
 * Certificate of Insurance (ACORD 25) PDF Generation Service.
 * Generates ACORD 25 compliant Certificate of Liability Insurance documents
 * with PolicyCenter policy period details, limits, and certificate holder endorsements.
 */
public class CertificateOfInsuranceService {
    private static final Logger LOGGER = Logger.getLogger(CertificateOfInsuranceService.class.getName());
    private static final CertificateOfInsuranceService instance = new CertificateOfInsuranceService();

    private CertificateOfInsuranceService() {
        LOGGER.log(Level.FINE, "→ CertificateOfInsuranceService initialized");
    }

    public static CertificateOfInsuranceService getInstance() {
        return instance;
    }

    public byte[] generateAcord25CoiPdf(PolicyPeriod period, String certificateHolder, String additionalInsuredNotes) throws IOException {
        LOGGER.log(Level.INFO, "Generating ACORD 25 Certificate of Insurance PDF for Policy: {0}", period.getPolicyNumber());

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // Document Header - ACORD 25
                writeTextLine(cs, boldFont, 16, 50, 750, "ACORD 25 - CERTIFICATE OF LIABILITY INSURANCE");
                writeTextLine(cs, regularFont, 9, 50, 736, "DATE (MM/DD/YYYY): " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()));
                writeTextLine(cs, regularFont, 8, 50, 724, "THIS CERTIFICATE IS ISSUED AS A MATTER OF INFORMATION ONLY AND CONFERS NO RIGHTS UPON THE CERTIFICATE HOLDER.");

                int y = 695;
                // Producer Information Box
                writeTextLine(cs, boldFont, 11, 50, y, "PRODUCER / AGENT:");
                y -= 14;
                writeTextLine(cs, regularFont, 10, 65, y, "Guidewire Global Insurance Services, LLC");
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, "100 California Street, San Francisco, CA 94111 | Phone: (800) 555-4843");

                // Insured Information Box
                y -= 24;
                writeTextLine(cs, boldFont, 11, 50, y, "INSURED:");
                y -= 14;
                String insuredName = period != null && period.getAccount() != null ? period.getAccount().getAccountHolderName() : "Valued Policyholder";
                String insuredAddr = period != null && period.getAccount() != null ? period.getAccount().getFormattedAddress() : "100 Main St, San Francisco, CA";
                writeTextLine(cs, regularFont, 10, 65, y, insuredName);
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, insuredAddr);

                // Insurer Letter Box
                y -= 24;
                writeTextLine(cs, boldFont, 11, 50, y, "COVERAGES & INSURER AFFORDING COVERAGE:");
                y -= 14;
                writeTextLine(cs, regularFont, 9, 65, y, "INSURER A: Guidewire Mutual PolicyCenter Assurance Co. (NAIC #10948)");

                // Policy Details Table Header
                y -= 24;
                writeTextLine(cs, boldFont, 10, 50, y, "POLICY DETAILS:");
                y -= 14;
                String polNum = period != null && period.getPolicyNumber() != null ? period.getPolicyNumber() : "POL-849102";
                String prodCode = period != null && period.getProductCode() != null ? period.getProductCode() : "PersonalAuto";
                writeTextLine(cs, regularFont, 9, 65, y, "Policy Number: " + polNum);
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, "Policy Type / Line of Business: " + prodCode);
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, "Effective Dates: " + new SimpleDateFormat("MM/dd/yyyy").format(new Date()) + " to " + new SimpleDateFormat("MM/dd/yyyy").format(new Date(System.currentTimeMillis() + 31536000000L)));

                // Coverage Limits Section
                y -= 24;
                writeTextLine(cs, boldFont, 10, 50, y, "LIMITS OF LIABILITY:");
                y -= 14;
                String bi = period != null ? period.getBodilyInjuryLimit() : "$250,000 / $500,000";
                String pd = period != null ? period.getPropertyDamageLimit() : "$100,000";
                String comp = period != null ? period.getComprehensiveDeductible() : "$500";
                String coll = period != null ? period.getCollisionDeductible() : "$500";
                writeTextLine(cs, regularFont, 9, 65, y, "EACH OCCURRENCE / BODILY INJURY: " + bi);
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, "PROPERTY DAMAGE: " + pd);
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, "COMPREHENSIVE DEDUCTIBLE: " + comp);
                y -= 12;
                writeTextLine(cs, regularFont, 9, 65, y, "COLLISION DEDUCTIBLE: " + coll);

                // Certificate Holder & Additional Insured Box
                y -= 28;
                writeTextLine(cs, boldFont, 11, 50, y, "CERTIFICATE HOLDER:");
                y -= 14;
                String certHolder = certificateHolder != null && !certificateHolder.trim().isEmpty() ? certificateHolder.trim() : "City & County of San Francisco, Department of Public Works";
                writeTextLine(cs, regularFont, 9, 65, y, certHolder);

                y -= 24;
                writeTextLine(cs, boldFont, 10, 50, y, "SPECIAL PROVISIONS / ADDITIONAL INSURED ENDORSEMENTS:");
                y -= 14;
                String addlNotes = additionalInsuredNotes != null && !additionalInsuredNotes.trim().isEmpty() ? additionalInsuredNotes.trim() : "Certificate Holder is listed as Primary & Non-Contributory Additional Insured per Form CG 20 10.";
                writeTextLine(cs, regularFont, 9, 65, y, addlNotes);

                // Cancellation Clause
                y -= 30;
                writeTextLine(cs, boldFont, 9, 50, y, "CANCELLATION:");
                y -= 12;
                writeTextLine(cs, regularFont, 8, 50, y, "SHOULD ANY OF THE ABOVE DESCRIBED POLICIES BE CANCELLED BEFORE THE EXPIRATION DATE THEREOF, NOTICE WILL BE DELIVERED IN ACCORDANCE WITH POLICY PROVISIONS.");

                // Authorized Representative Signature
                writeTextLine(cs, boldFont, 10, 350, 80, "AUTHORIZED REPRESENTATIVE:");
                writeTextLine(cs, regularFont, 10, 350, 65, "/s/ Guidewire Digital Agent");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void writeTextLine(PDPageContentStream cs, PDType1Font font, int fontSize, int x, int y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}
