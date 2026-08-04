package com.guidewire.pc.constants;

import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class PCConstants {
    private static final Logger LOGGER = Logger.getLogger(PCConstants.class.getName());

    private PCConstants() {
        LOGGER.log(Level.FINE, "→ PCConstants.PCConstants");}

    // Policy & Period Statuses
    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_QUOTED = "Quoted";
    public static final String STATUS_BOUND = "Bound";
    public static final String STATUS_ISSUED = "Issued";
    public static final String STATUS_CANCELED = "Canceled";
    public static final String STATUS_EXPIRED = "Expired";

    // Account Statuses
    public static final String ACCOUNT_STATUS_ACTIVE = "Active";
    public static final String ACCOUNT_STATUS_PENDING = "Pending";
    public static final String ACCOUNT_STATUS_CLOSED = "Closed";

    // Job Types
    public static final String JOB_TYPE_SUBMISSION = "Submission";
    public static final String JOB_TYPE_POLICY_CHANGE = "PolicyChange";
    public static final String JOB_TYPE_CANCELLATION = "Cancellation";
    public static final String JOB_TYPE_REINSTATEMENT = "Reinstatement";
    public static final String JOB_TYPE_RENEWAL = "Renewal";
    public static final String JOB_TYPE_REWRITE = "Rewrite";
    public static final String JOB_TYPE_REWRITE_NEW_ACCOUNT = "RewriteNewAccount";

    // Product Codes
    public static final String PRODUCT_PERSONAL_AUTO = "PersonalAuto";
    public static final String PRODUCT_COMMERCIAL_AUTO = "CommercialAuto";
    public static final String PRODUCT_COMMERCIAL_PROPERTY = "CommercialProperty";
    public static final String PRODUCT_GENERAL_LIABILITY = "GeneralLiability";
    public static final String PRODUCT_WORKERS_COMP = "WorkersComp";
    public static final String PRODUCT_INLAND_MARINE = "InlandMarine";

    // UW Issue Severities & Statuses
    public static final String UW_SEVERITY_BLOCKING_QUOTE = "BlockingQuote";
    public static final String UW_SEVERITY_BLOCKING_BIND = "BlockingBind";
    public static final String UW_SEVERITY_INFORMATIONAL = "Informational";
    public static final String UW_STATUS_OPEN = "Open";
    public static final String UW_STATUS_APPROVED = "Approved";
    public static final String UW_STATUS_REJECTED = "Rejected";

    // Charge Patterns
    public static final String CHARGE_BASE_PREMIUM = "BasePremium";
    public static final String CHARGE_BODILY_INJURY = "BodilyInjuryCoverage";
    public static final String CHARGE_COLLISION = "CollisionCoverage";
    public static final String CHARGE_COMPREHENSIVE = "ComprehensiveCoverage";
    public static final String CHARGE_MULTI_POLICY_DISCOUNT = "MultiPolicyDiscount";
    public static final String CHARGE_STATE_TAX = "StateTax";
    public static final String CHARGE_POLICY_FEE = "PolicyFee";

    // Financial & Underwriting Defaults
    public static final BigDecimal DEFAULT_BASE_PREMIUM = new BigDecimal("2500.00");
    public static final BigDecimal DEFAULT_HIGH_LOSS_THRESHOLD = new BigDecimal("10000.00");
    public static final double SHORT_RATE_RETENTION_FACTOR = 0.90;
    public static final double MULTI_POLICY_DISCOUNT_FACTOR = 0.15;

    // Default Security & Credentials
    public static final String DEFAULT_SU_USER = "su";
    public static final String DEFAULT_SU_PASSWORD = "gw";
    public static final String DEFAULT_SESSION_TOKEN_PREFIX = "SESSIONID=gw_su_session";
    public static final String DEFAULT_PRODUCER_CODE = "PR-10928";
    public static final int DEFAULT_SERVER_PORT = 8085;
}
