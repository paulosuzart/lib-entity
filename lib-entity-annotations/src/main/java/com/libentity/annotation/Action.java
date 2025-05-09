package com.libentity.annotation;

import java.lang.annotation.*;

/**
 * Defines an action that can be performed on an entity.
 * <p>
 * Usage: Used inside the actions array in @EntityDefinition.
 * <ul>
 *   <li><b>name</b>: Action name (required).</li>
 *   <li><b>handler</b>: Handler class (required).</li>
 *   <li><b>command</b>: Command type (required).</li>
 *   <li><b>description</b>: Description (optional).</li>
 *   <li><b>visible</b>: Whether the action is visible (default: true).</li>
 *   <li><b>allowedStates</b>: States in which the action is allowed (optional).</li>
 * </ul>
 * Example:
 * <pre>
 * {@code @Action(name = "submitPayment", handler = SubmitHandler.class, command = SubmitCommand.class, allowedStates = {"DRAFT"})}
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Action {
    String name();

    String description() default "";

    Class<?> handler();

    String[] allowedStates() default {};

    Class<?> command();
}
