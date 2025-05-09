package com.libentity.jooqsupport.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface JooqFilterField {
    String field();

    Comparator[] comparators();

    boolean virtual() default false;

    Class<?> mapper() default Void.class;
}
