package com.libentity.jooqsupport;

import static org.assertj.core.api.Assertions.assertThat;

import com.libentity.core.filter.FieldFilterType;
import com.libentity.core.filter.FilterDefinition;
import com.libentity.core.filter.RangeFilter;
import java.util.*;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

public class JooqFilterSupportTest {

    static class TestFilter {
        public Integer age;
        public String name;
        public RangeFilter<Integer> ageRange;
        public List<String> tags;
    }

    private static final Field<Integer> AGE_FIELD = DSL.field("age", Integer.class);
    private static final Field<String> NAME_FIELD = DSL.field("name", String.class);
    private static final Field<String> TAGS_FIELD = DSL.field("tags", String.class);

    private static final Map<String, Field<?>> FIELD_MAPPING = Map.of(
            "age", AGE_FIELD,
            "name", NAME_FIELD,
            "tags", TAGS_FIELD);

    private static final FilterDefinition<TestFilter> FILTER_DEFINITION = new FilterDefinition<>(
            "TestFilter",
            TestFilter.class,
            Map.of(
                    "age", Set.of(FieldFilterType.EQ, FieldFilterType.GT, FieldFilterType.LT),
                    "name", Set.of(FieldFilterType.EQ),
                    "tags", Set.of(FieldFilterType.IN),
                    "ageRange", Set.of(FieldFilterType.GT, FieldFilterType.LT, FieldFilterType.EQ)));

    @Test
    void testBuildCondition_withEqComparator() {
        TestFilter filter = new TestFilter();
        filter.age = 30;
        Condition condition = JooqFilterSupport.buildCondition(filter, FILTER_DEFINITION, FIELD_MAPPING);
        assertThat(condition.toString()).contains("age = 30");
    }

    @Test
    void testBuildCondition_withRangeComparators() {
        TestFilter filter = new TestFilter();
        filter.age = 30;
        Condition condition = JooqFilterSupport.buildCondition(filter, FILTER_DEFINITION, FIELD_MAPPING);
        assertThat(condition.toString()).contains("age = 30");
    }

    @Test
    void testBuildCondition_withInComparator() {
        TestFilter filter = new TestFilter();
        filter.tags = Arrays.asList("admin", "user");
        Condition condition = JooqFilterSupport.buildCondition(filter, FILTER_DEFINITION, FIELD_MAPPING);
        assertThat(condition.toString()).contains("tags in (");
    }

    @Test
    void testBuildCondition_withNullValues_ignoresNulls() {
        TestFilter filter = new TestFilter();
        Condition condition = JooqFilterSupport.buildCondition(filter, FILTER_DEFINITION, FIELD_MAPPING);
        assertThat(condition.toString()).isEqualTo("true"); // DSL.trueCondition()
    }

    @Test
    void testBuildCondition_withMultipleFields() {
        TestFilter filter = new TestFilter();
        filter.age = 25;
        filter.name = "Alice";
        Condition condition = JooqFilterSupport.buildCondition(filter, FILTER_DEFINITION, FIELD_MAPPING);
        assertThat(condition.toString()).contains("age = 25").contains("name = 'Alice'");
    }
}
