package com.guidewire.pc.util;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class DisplayProperties {
    private static final Logger LOGGER = Logger.getLogger(DisplayProperties.class.getName());

    private DisplayProperties() {
        LOGGER.log(Level.FINE, "→ DisplayProperties.DisplayProperties");}

    // REST & Error API Messages
    public static final String ERR_JOB_NOT_FOUND = "Requested Policy Job/Submission was not found.";
    public static final String ERR_POLICY_NOT_FOUND = "Requested Policy Period was not found.";
    public static final String ERR_ACCOUNT_NOT_FOUND = "Requested Account was not found.";
    public static final String ERR_UNAUTHORIZED = "Unauthorized: Invalid or missing API session token.";
    public static final String ERR_UNDERWRITING_REFERRAL_HOLD = "Underwriting Referral Hold: Account has open loss claims exceeding $10,000.";

    // Success & Status Messages
    public static final String MSG_POLICY_BOUND_SUCCESS = "Policy successfully bound and issued.";
    public static final String MSG_GOSU_RELOAD_SUCCESS = "Gosu script directory hot-reloaded successfully.";
    public static final String MSG_DB_RESET_SUCCESS = "Database reset to clean sample seed data.";
    public static final String MSG_WEBHOOK_DISPATCH_SUCCESS = "Dispatched webhook notification event across Virtual Threads.";
}
