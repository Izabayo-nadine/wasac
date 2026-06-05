package com.wasac.billing.validation;

/**
 * Shared Jakarta Bean Validation patterns for the utility billing system.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final String NATIONAL_ID = "^\\d{16}$";
    public static final String NATIONAL_ID_MESSAGE = "National ID must be exactly 16 digits";

    public static final String PHONE = "^(078|073|072)\\d{7}$";
    public static final String PHONE_MESSAGE = "Phone number must be 10 digits starting with 078, 073, or 072";
}
