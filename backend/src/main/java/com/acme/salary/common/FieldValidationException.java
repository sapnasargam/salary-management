package com.acme.salary.common;

/**
 * A business-rule violation tied to one request field (e.g. unknown country, e-mail already in use),
 * reported to clients the same way as bean-validation errors so the UI can show it next to the input.
 */
public class FieldValidationException extends RuntimeException {

    private final String field;
    private final boolean conflict;

    private FieldValidationException(String field, String message, boolean conflict) {
        super(message);
        this.field = field;
        this.conflict = conflict;
    }

    /** The value is invalid on its own (HTTP 400). */
    public static FieldValidationException invalid(String field, String message) {
        return new FieldValidationException(field, message, false);
    }

    /** The value clashes with existing data (HTTP 409). */
    public static FieldValidationException conflict(String field, String message) {
        return new FieldValidationException(field, message, true);
    }

    public String getField() {
        return field;
    }

    public boolean isConflict() {
        return conflict;
    }
}
