package com.libentity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a validator for state transitions.
 * <p>
 * Usage: Place on a class or method.
 * <ul>
 *   <li><b>entity</b>: Entity name(s) this validator applies to (required).</li>
 *   <li><b>from</b>: From-state name (required).</li>
 *   <li><b>to</b>: To-state name (required).</li>
 * </ul>
 * Validator method must have exactly 4 parameters: (from-state, to-state, request, ValidationContext).
 * Example:
 * <pre>
 * {@code
 * @TransitionValidator(entity = {"Payment"}, from = "DRAFT", to = "APPROVED")
 * public void validate(PaymentState from, PaymentState to, PaymentRequest req, ValidationContext ctx) { }
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransitionValidator {
    String[] entity();

    String from();

    String to();
}
