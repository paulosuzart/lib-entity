package com.libentity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the predicate for action availability.
 * <p>
 * Usage: Place on a method in the action handler class.
 * <ul>
 *   <li>Method should return a boolean indicating if the action is available in the current context.</li>
 * </ul>
 * Example:
 * <pre>
 * {@code
 * @OnlyIf
 * public boolean isAvailable(PaymentState state, PaymentRequest req) { return true; }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyIf {}
