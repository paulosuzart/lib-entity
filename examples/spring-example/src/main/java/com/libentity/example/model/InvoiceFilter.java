package com.libentity.example.model;

import com.libentity.core.filter.RangeFilter;
import com.libentity.jooqsupport.annotation.*;
import java.math.BigDecimal;
import java.time.chrono.ChronoLocalDate;
import java.util.Set;
import lombok.Data;

@Data
@JooqFilter(
        tableClass = "org.jooq.generated.tables.Invoice",
        tableVar = "INVOICE",
        defaultSort = @JooqDefaultSort(field = "dueDate", direction = SortDirection.DESC))
public class InvoiceFilter {
    @JooqFilterField(
            field = "AMOUNT",
            comparators = {Comparator.GT, Comparator.LT, Comparator.GTE, Comparator.LTE, Comparator.EQ})
    private RangeFilter<BigDecimal> amount;

    @JooqFilterField(
            field = "VAT",
            comparators = {Comparator.EQ})
    private RangeFilter<BigDecimal> vat;

    @JooqFilterField(
            field = "DUE_DATE",
            comparators = {Comparator.LT, Comparator.GTE})
    private RangeFilter<ChronoLocalDate> dueDate;

    @JooqFilterField(
            field = "READY_FOR_APPROVAL",
            comparators = {Comparator.BOOLEAN})
    private Boolean readyForApproval;

    @JooqFilterField(
            field = "EMPLOYEE_ID",
            comparators = {Comparator.IN})
    private Set<String> employeeIdIn;

    @JooqFilterField(
            virtual = true,
            field = "SUBMITTED_BY_ME",
            comparators = {Comparator.EQ})
    private Boolean submittedByMe;

    // Pagination and sorting fields (optional, for future extension)
    private Integer limit;
    private Integer offset;
    private String cursor;
    // Sorting would be handled via generated metadata
}
// Touch for annotation processing
