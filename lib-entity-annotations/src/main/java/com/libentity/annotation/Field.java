package com.libentity.annotation;

import java.lang.annotation.*;

/**
 * Declares a field for an entity in the annotation-based DSL.
 * <p>
 * Usage: Used inside the fields array in @EntityDefinition.
 * <ul>
 *   <li><b>name</b>: Field name (required).</li>
 *   <li><b>type</b>: Field type (required).</li>
 *   <li><b>required</b>: Whether the field is required (default: false).</li>
 *   <li><b>inStateValidators</b>: (Optional) Array of in-state validator classes for this field.</li>
 *   <li><b>transitionValidators</b>: (Optional) Array of transition validator classes for this field.</li>
 * </ul>
 * Example:
 * <pre>
 * {@code @Field(name = "amount", type = int.class, inStateValidators = {AmountInStateValidator.class})}
 * </pre>
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Field {
    String name();

    Class<?> type();

    boolean required() default false;

    Class<?>[] inStateValidators() default {};

    Class<?>[] transitionValidators() default {};
}
