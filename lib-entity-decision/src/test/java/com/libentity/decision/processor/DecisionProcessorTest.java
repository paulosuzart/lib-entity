package com.libentity.decision.processor;

import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaFileObjects.forSourceString;

import java.util.List;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

class DecisionProcessorTest {

    @Test
    void generatesMetaClassForAnnotatedFilter() {
        JavaFileObject filterSource = forSourceString(
                "com.example.InvoiceInput",
                """
            package com.example;

            import com.libentity.decision.DecisionInput;
            import com.libentity.decision.Rule;
            import java.math.BigDecimal;
            import java.time.LocalDate;
            import java.util.UUID;

            @DecisionInput
            public class InvoiceInput {
                Rule<UUID> isVatExempt;
                Rule<LocalDate> isDateSet;
                Rule<BigDecimal> amount;
                Rule<UUID> isApproved;
            }
            """);
        var compilation = javac().withProcessors(new DecisionProcessor()).compile(List.of(filterSource));
        assertThat(compilation.errors()).isEmpty();
        assertThat(compilation.generatedSourceFile("com.example.InvoiceInputValue"))
                .isPresent();
    }
}
