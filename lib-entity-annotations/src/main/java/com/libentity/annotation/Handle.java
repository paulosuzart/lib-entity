package com.libentity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the handler for an action.
 * <p>
 * Usage: Place on a method in the action handler class.
 * <ul>
 *   <li>Method must have exactly 4 parameters: (state enum, request, command, StateMutator).</li>
 *   <li>See README for full signature requirements.</li>
 * </ul>
 * Example:
 * <pre>
 * {@code
 * @Handle
 * public void handle(PaymentState state, PaymentRequest req, SubmitPaymentCommand cmd, StateMutator mutator) { }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handle {}
