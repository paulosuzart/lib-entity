package com.libentity.example.repository;

import static org.jooq.generated.tables.Invoice.INVOICE;

import com.libentity.example.model.InvoiceFilter;
import com.libentity.example.model.InvoiceFilterJooqMeta.InvoiceFilterJooqMetaVirtualMapperFactory;
import com.libentity.jooqsupport.VirtualConditionMapper;
import com.libentity.jooqsupport.annotation.Comparator;
import java.util.List;

/**
 * Virtual mapper factory for InvoiceFilter. This is responsible for a virtual
 * condition mapper that can be used to filter invoices by the user ID.
 */
public class InvoiceFilterVirtualMapperFactoryImpl implements InvoiceFilterJooqMetaVirtualMapperFactory {
    private final String userId;

    public InvoiceFilterVirtualMapperFactoryImpl(String userId) {
        this.userId = userId;
    }

    @Override
    public VirtualConditionMapper<InvoiceFilter> getSubmittedByMeMapper(List<Comparator> comparators) {
        if (comparators.contains(Comparator.EQ)) {
            return f -> INVOICE.EMPLOYEE_ID.eq(userId);
        }
        return f -> null;
    }
}
