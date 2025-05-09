package com.example;

import org.jooq.SortField;
import org.jooq.impl.DSL;

public class UserTable {
    public static final USER USER = new USER();

    public static class USER {
        public SortField<?> asc() {
            return DSL.field("dummy").asc();
        }

        public SortField<?> desc() {
            return DSL.field("dummy").desc();
        }
    }

    // Remove test dummy SortField to avoid masking real jOOQ class
    // public static class SortField extends org.jooq.SortField {}
}
