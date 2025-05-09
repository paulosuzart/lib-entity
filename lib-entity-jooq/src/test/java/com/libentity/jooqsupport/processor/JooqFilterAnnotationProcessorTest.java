package com.libentity.jooqsupport.processor;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forSourceString;

import java.util.List;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

public class JooqFilterAnnotationProcessorTest {
    @Test
    void generatesMetaClassForAnnotatedFilter() {
        JavaFileObject filterSource = forSourceString(
                "com.example.UserFilter",
                """
            package com.example;
            import com.libentity.jooqsupport.annotation.JooqFilter;
            import com.libentity.jooqsupport.annotation.JooqFilterField;
            @JooqFilter(tableClass = \"UserTable\", tableVar = \"USER\")
            public class UserFilter {
                @JooqFilterField(field = \"id\", comparators = {com.libentity.jooqsupport.annotation.Comparator.EQ})
                public Long id;
            }
            """);
        var compilation =
                javac().withProcessors(new JooqFilterAnnotationProcessor()).compile(List.of(filterSource));
        assertThat(compilation.errors()).isEmpty();
        assertThat(compilation.generatedSourceFile("com.example.UserFilterJooqMeta"))
                .isPresent();
    }

    @Test
    void failsOnMissingTableClass() {
        JavaFileObject filterSource = forSourceString(
                "com.example.InvalidFilter",
                """
            package com.example;
            import com.libentity.jooqsupport.annotation.JooqFilter;
            import com.libentity.jooqsupport.annotation.JooqFilterField;
            @JooqFilter(tableVar = \"USER\") // missing tableClass
            public class InvalidFilter {
                @JooqFilterField(field = \"id\", comparators = {com.libentity.jooqsupport.annotation.Comparator.EQ})
                public Long id;
            }
            """);
        var compilation =
                javac().withProcessors(new JooqFilterAnnotationProcessor()).compile(List.of(filterSource));
        assertThat(compilation.errors()).isNotEmpty();
    }
}
