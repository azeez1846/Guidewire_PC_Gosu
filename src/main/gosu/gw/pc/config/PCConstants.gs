package gw.pc.config

class PCConstants {
  // Policy & Period Statuses
  public static final var STATUS_DRAFT: String = "Draft"
  public static final var STATUS_QUOTED: String = "Quoted"
  public static final var STATUS_BOUND: String = "Bound"
  public static final var STATUS_ISSUED: String = "Issued"
  public static final var STATUS_CANCELED: String = "Canceled"
  public static final var STATUS_EXPIRED: String = "Expired"

  // Account Statuses
  public static final var ACCOUNT_STATUS_ACTIVE: String = "Active"
  public static final var ACCOUNT_STATUS_PENDING: String = "Pending"
  public static final var ACCOUNT_STATUS_CLOSED: String = "Closed"

  // Job Types
  public static final var JOB_TYPE_SUBMISSION: String = "Submission"
  public static final var JOB_TYPE_POLICY_CHANGE: String = "PolicyChange"
  public static final var JOB_TYPE_CANCELLATION: String = "Cancellation"
  public static final var JOB_TYPE_REINSTATEMENT: String = "Reinstatement"
  public static final var JOB_TYPE_RENEWAL: String = "Renewal"
  public static final var JOB_TYPE_AUDIT: String = "Audit"

  // Audit Statuses
  public static final var AUDIT_STATUS_DRAFT: String = "Draft"
  public static final var AUDIT_STATUS_IN_PROCESS: String = "InProcess"
  public static final var AUDIT_STATUS_COMPLETE: String = "Complete"
  public static final var AUDIT_STATUS_CLOSED: String = "Closed"

  // Product Codes
  public static final var PRODUCT_PERSONAL_AUTO: String = "PersonalAuto"
  public static final var PRODUCT_COMMERCIAL_AUTO: String = "CommercialAuto"
  public static final var PRODUCT_COMMERCIAL_PROPERTY: String = "CommercialProperty"
  public static final var PRODUCT_GENERAL_LIABILITY: String = "GeneralLiability"
  public static final var PRODUCT_WORKERS_COMP: String = "WorkersComp"

  // Rating Charge Patterns
  public static final var CHARGE_BASE_PREMIUM: String = "BasePremium"
  public static final var CHARGE_BODILY_INJURY: String = "BodilyInjuryCoverage"
  public static final var CHARGE_COLLISION: String = "CollisionCoverage"
  public static final var CHARGE_COMPREHENSIVE: String = "ComprehensiveCoverage"
  public static final var CHARGE_MULTI_POLICY_DISCOUNT: String = "MultiPolicyDiscount"
  public static final var CHARGE_STATE_TAX: String = "StateTax"
  public static final var CHARGE_POLICY_FEE: String = "PolicyFee"

  // Default Financial & Rating Constants
  public static final var DEFAULT_PRO_RATA_CANCEL_FEE_PERCENT: double = 0.0
  public static final var SHORT_RATE_RETENTION_FACTOR: double = 0.90
  public static final var MULTI_POLICY_DISCOUNT_PERCENT: double = 0.15
  public static final var HIGH_LOSS_CLAIM_THRESHOLD: double = 10000.00
}
