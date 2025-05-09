package com.libentity.annotation;

import java.lang.annotation.*;

/**
 * Defines an entity type for the annotation-based DSL.
 * <p>
 * Usage: Place on a class to declare an entity and its structure.
 * <ul>
 *   <li><b>name</b>: Unique name for the entity.</li>
 *   <li><b>stateEnum</b>: Enum class for possible states.</li>
 *   <li><b>fields</b>: Array of @Field definitions.</li>
 *   <li><b>actions</b>: (Optional) Array of @Action definitions.</li>
 *   <li><b>inStateValidators</b>: (Optional) Classes for in-state validation.</li>
 *   <li><b>transitionValidators</b>: (Optional) Classes for transition validation.</li>
 * </ul>
 * Example:
 * <pre>
 * {@code
 * @EntityDefinition(
 *   name = "Payment",
 *   stateEnum = PaymentState.class,
 *   fields = {@Field(name = "amount", type = int.class)},
 *   actions = {@Action(name = "submitPayment", handler = SubmitHandler.class, command = SubmitCommand.class)}
 * )
 * public class Payment {}
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityDefinition {
    String name();

    Class<? extends Enum<?>> stateEnum();

    Field[] fields();

    Action[] actions() default {};

    Class<?>[] inStateValidators() default {};

    Class<?>[] transitionValidators() default {};
}
