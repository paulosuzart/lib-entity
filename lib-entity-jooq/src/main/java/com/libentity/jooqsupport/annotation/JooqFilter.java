package com.libentity.jooqsupport.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface JooqFilter {
    JooqDefaultSort defaultSort() default @JooqDefaultSort(field = "", direction = SortDirection.ASC);

    String tableClass();

    String tableVar();
}
