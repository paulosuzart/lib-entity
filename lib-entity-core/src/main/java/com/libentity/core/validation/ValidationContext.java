package com.libentity.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/** Context for collecting validation errors during entity validation. */
public class ValidationContext {
    @Getter
    private final List<ValidationError> errors = new ArrayList<>();

    /** Add a validation error with a code and message. */
    public void addError(String code, String message) {
        errors.add(ValidationError.builder().code(code).defaultMessage(message).build());
    }

    /** Add a validation error with parameters. */
    public void addError(String code, String message, Map<String, Object> parameters) {
        errors.add(ValidationError.builder()
                .code(code)
                .defaultMessage(message)
                .parameters(parameters)
                .build());
    }

    /** Check if there are any validation errors. */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
