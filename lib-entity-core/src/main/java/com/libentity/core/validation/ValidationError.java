package com.libentity.core.validation;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

/** Represents a validation error with a code, message, and optional parameters. */
@Value
@Builder
public class ValidationError {
    String code;
    String defaultMessage;
    Map<String, Object> parameters;
}
