package com.libentity.jooqsupport;

import org.jooq.Condition;

/**
 * Interface for mapping a filter object to a JOOQ Condition for virtual fields.
 * @param <F> The filter type
 */
public interface VirtualConditionMapper<F> {
    Condition map(F filter);
}
