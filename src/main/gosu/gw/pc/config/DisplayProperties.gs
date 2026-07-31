package gw.pc.config

class DisplayProperties {
  // Validation Error Messages
  public static final var ERR_ACCOUNT_NUMBER_REQUIRED: String = "Account Number is required."
  public static final var ERR_ACCOUNT_HOLDER_NAME_REQUIRED: String = "Account Holder Name is required."
  public static final var ERR_EFFECTIVE_DATE_REQUIRED: String = "Policy Effective Date is required."
  public static final var ERR_INVALID_PRODUCT_CODE: String = "Invalid product code specified for submission."
  public static final var ERR_OPEN_CLAIMS_REFERRAL_HOLD: String = "Underwriting Hold: Account has open claims exceeding threshold."

  // User Notification Strings
  public static final var MSG_POLICY_BOUND_SUCCESS: String = "Policy period has been successfully bound and issued."
  public static final var MSG_ENDORSEMENT_CREATED: String = "Mid-term policy change endorsement job initialized."
  public static final var MSG_CANCELLATION_COMPLETED: String = "Policy has been canceled and refund calculation completed."
  public static final var MSG_REINSTATEMENT_COMPLETED: String = "Policy reinstated to active status."
}
