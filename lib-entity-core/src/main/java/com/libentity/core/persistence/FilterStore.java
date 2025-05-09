package com.libentity.core.persistence;

import java.util.List;

public interface FilterStore<E, F> {
    List<E> findByFilter(F filter);
}
