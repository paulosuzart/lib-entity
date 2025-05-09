package com.libentity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a validator to check entity state before an action.
 * <p>
 * Usage: Place on a class or method.
 * <ul>
 *   <li><b>entity</b>: Entity name(s) this validator applies to (required).</li>
 *   <li><b>state</b>: State name this validator applies to (required).</li>
 * </ul>
 * Validator method must have exactly 3 parameters: (state enum, request, ValidationContext).
 * Example:
 * <pre>
 * {@code
 * @InStateValidator(entity = {"Payment"}, state = "DRAFT")
 * public void validate(PaymentState state, PaymentRequest req, ValidationContext ctx) { }
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InStateValidator {
    String[] entity();

    String state();
}
