package com.libentity.core.validation;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("Validation failed: " + errors);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
