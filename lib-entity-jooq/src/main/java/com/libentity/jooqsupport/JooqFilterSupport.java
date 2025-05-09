package com.libentity.jooqsupport;

import com.libentity.core.filter.FieldFilterType;
import com.libentity.core.filter.FilterDefinition;
import com.libentity.core.filter.RangeFilter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.jooq.Condition;
import org.jooq.impl.DSL;

/**
 * Utility class for building JOOQ {@link Condition} objects from LibEntity filter definitions.
 * <p>
 * This class helps translate high-level filter objects and their supported operations
 * (like EQ, IN, GT, etc.) into JOOQ conditions for querying the database.
 * </p>
 */
@SuppressWarnings("unchecked")
public class JooqFilterSupport {
    /**
     * Builds a JOOQ {@link Condition} based on the provided filter object, filter definition, and field mapping.
     *
     * @param filter         The filter object containing filter values
     * @param definition     The filter definition specifying supported fields and types
     * @param fieldMapping   Mapping from logical field names to JOOQ fields
     * @return A JOOQ {@link Condition} representing the filter criteria
     */
    public static <F> Condition buildCondition(
            F filter, FilterDefinition<F> definition, Map<String, org.jooq.Field<?>> fieldMapping) {
        Condition condition = DSL.trueCondition();
        for (Map.Entry<String, Set<FieldFilterType>> entry :
                definition.getSupportedFields().entrySet()) {
            String fieldName = entry.getKey();
            Set<FieldFilterType> filterTypes = entry.getValue();
            try {
                Field filterField = filter.getClass().getDeclaredField(fieldName);
                filterField.setAccessible(true);
                Object filterValue = filterField.get(filter);
                if (filterValue == null) continue;
                org.jooq.Field<?> jooqField = fieldMapping.get(fieldName);
                if (jooqField == null) continue;
                condition = condition.and(buildFieldCondition(filterValue, filterTypes, jooqField));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // ignore, not filterable
            }
        }
        return condition;
    }

    /**
     * Builds a JOOQ condition for a single field based on its value and supported filter types.
     */
    private static Condition buildFieldCondition(
            Object filterValue, Set<FieldFilterType> filterTypes, org.jooq.Field<?> jooqField) {
        Condition condition = DSL.trueCondition();
        if (filterValue instanceof RangeFilter<?> rf) {
            condition = condition.and(buildRangeFilterCondition(rf, filterTypes, jooqField));
        } else if (filterValue instanceof Boolean b) {
            if (filterTypes.contains(FieldFilterType.BOOLEAN)) {
                condition = condition.and(((org.jooq.Field<Boolean>) jooqField).eq(b));
            }
        } else {
            if (filterTypes.contains(FieldFilterType.EQ)) {
                condition = condition.and(((org.jooq.Field<Object>) jooqField).eq(filterValue));
            }
            if (filterTypes.contains(FieldFilterType.IN) && filterValue instanceof Collection<?> c) {
                condition = condition.and(((org.jooq.Field<Object>) jooqField).in(c));
            }
        }
        return condition;
    }

    /**
     * Builds a JOOQ condition for a RangeFilter (GT, GTE, LT, LTE, EQ).
     */
    private static Condition buildRangeFilterCondition(
            RangeFilter<?> rf, Set<FieldFilterType> filterTypes, org.jooq.Field<?> jooqField) {
        Condition condition = DSL.trueCondition();
        for (FieldFilterType type : filterTypes) {
            switch (type) {
                case GT -> {
                    if (rf.getGt() != null)
                        condition = condition.and(
                                ((org.jooq.Field<Comparable<Object>>) jooqField).gt((Comparable<Object>) rf.getGt()));
                }
                case GTE -> {
                    if (rf.getGte() != null)
                        condition = condition.and(
                                ((org.jooq.Field<Comparable<Object>>) jooqField).ge((Comparable<Object>) rf.getGte()));
                }
                case LT -> {
                    if (rf.getLt() != null)
                        condition = condition.and(
                                ((org.jooq.Field<Comparable<Object>>) jooqField).lt((Comparable<Object>) rf.getLt()));
                }
                case LTE -> {
                    if (rf.getLte() != null)
                        condition = condition.and(
                                ((org.jooq.Field<Comparable<Object>>) jooqField).le((Comparable<Object>) rf.getLte()));
                }
                case EQ -> {
                    if (rf.getEq() != null)
                        condition = condition.and(((org.jooq.Field<Object>) jooqField).eq(rf.getEq()));
                }
                default -> {}
            }
        }
        return condition;
    }
}
