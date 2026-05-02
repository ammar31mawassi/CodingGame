package com.codeescape.validation;

public class ValidationResult {
    private final boolean valid;
    private final String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public static ValidationResult success(String message) {
        return new ValidationResult(true, message);
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    @Override
    public String toString() {
        return message;
    }
}
