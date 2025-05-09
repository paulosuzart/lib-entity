package com.libentity.core.filter;

import lombok.Data;

@Data
public class RangeFilter<T extends Comparable<T>> {
    private T gt;
    private T gte;
    private T lt;
    private T lte;
    private T eq;
}
