package com.libentity.jooqsupport.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface JooqDefaultSort {
    String field();

    SortDirection direction();
}
