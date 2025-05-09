package com.libentity.core.filter;

import java.util.Map;
import java.util.Set;

public class FilterDefinition<F> {
    private final String name;
    private final Class<F> filterClass;
    private final Map<String, Set<FieldFilterType>> supportedFields;

    public FilterDefinition(String name, Class<F> filterClass, Map<String, Set<FieldFilterType>> supportedFields) {
        this.name = name;
        this.filterClass = filterClass;
        this.supportedFields = supportedFields;
    }

    public String getName() {
        return name;
    }

    public Class<F> getFilterClass() {
        return filterClass;
    }

    public Map<String, Set<FieldFilterType>> getSupportedFields() {
        return supportedFields;
    }
}
