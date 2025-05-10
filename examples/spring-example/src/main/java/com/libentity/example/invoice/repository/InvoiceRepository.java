package com.libentity.example.invoice.repository;

import static org.jooq.generated.tables.Invoice.INVOICE;

import com.libentity.core.persistence.EntityStore;
import com.libentity.core.persistence.FilterStore;
import com.libentity.example.invoice.model.Invoice;
import com.libentity.example.invoice.model.InvoiceFilter;
import com.libentity.example.invoice.model.InvoiceFilterJooqMeta;
import com.libentity.example.invoice.model.InvoiceFilterJooqMeta.InvoiceFilterJooqMetaVirtualMapperFactory;
import com.libentity.example.invoice.model.InvoiceState;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.generated.tables.records.InvoiceRecord;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InvoiceRepository implements EntityStore<Invoice, Long>, FilterStore<Invoice, InvoiceFilter> {
    private final DSLContext dsl;

    /**
     * Loads the invoice with the given ID.
     *
     * @param id the ID of the invoice to load
     * @return the invoice with the given ID, or null if not found
     */
    @Override
    public Invoice loadById(Long id) {
        InvoiceRecord record = dsl.selectFrom(INVOICE).where(INVOICE.ID.eq(id)).fetchOne();
        return record != null ? toInvoice(record) : null;
    }

    /**
     * Saves the given invoice to the database.
     *
     * @param invoice the invoice to save
     */
    @Override
    public void save(Invoice invoice) {
        InvoiceRecord record = dsl.newRecord(INVOICE);
        fromInvoice(record, invoice);
        // Upsert logic: insert or update if exists
        InvoiceRecord persisted = dsl.insertInto(INVOICE)
                .set(record)
                .onConflict(INVOICE.ID)
                .doUpdate()
                .set(record)
                .returning(INVOICE.ID)
                .fetchOne();
        if (persisted != null) {
            invoice.setId(persisted.getId());
        }
    }

    @Override
    public List<Invoice> findByFilter(InvoiceFilter filter) {
        return findByFilter(filter, null);
    }

    /**
     * Finds invoices by filter.
     *
     * @param filter the filter to apply
     * @param userId the user ID to filter by
     * @return the list of invoices that match the filter
     */
    public List<Invoice> findByFilter(InvoiceFilter filter, String userId) {
        InvoiceFilterJooqMetaVirtualMapperFactory factory = new InvoiceFilterVirtualMapperFactoryImpl(userId);
        var condition = InvoiceFilterJooqMeta.toCondition(filter, factory);
        var sortFields = InvoiceFilterJooqMeta.getSortFields(filter);
        Integer limit = filter.getLimit();
        Integer offset = filter.getOffset();
        var select = dsl.selectFrom(INVOICE).where(condition).orderBy(sortFields.toArray(new SortField[0]));
        if (limit != null && offset != null) {
            return select.limit(limit).offset(offset).fetch().map(this::toInvoice);
        } else if (limit != null) {
            return select.limit(limit).fetch().map(this::toInvoice);
        } else if (offset != null) {
            return select.offset(offset).fetch().map(this::toInvoice);
        } else {
            return select.fetch().map(this::toInvoice);
        }
    }

    private Invoice toInvoice(InvoiceRecord record) {
        Invoice invoice = new Invoice();
        invoice.setId(record.getId());
        invoice.setEmployeeId(record.getEmployeeId());
        invoice.setAmount(record.getAmount());
        invoice.setVat(record.getVat());
        invoice.setDueDate(asLocalDate(record.getDueDate()));
        invoice.setSubmittedAt(asLocalDate(record.getSubmittedAt()));
        invoice.setSubmitterId(record.getSubmitterId());
        invoice.setSubmitterDeviceId(record.getSubmitterDeviceId());
        invoice.setApprovalDate(asLocalDate(record.getApprovalDate()));
        invoice.setApproverId(record.getApproverId());
        invoice.setRejectionReason(record.getRejectionReason());
        invoice.setRejectedBy(record.getRejectedBy());
        invoice.setRejectionDate(asLocalDate(record.getRejectionDate()));
        invoice.setReceiptNumber(record.getReceiptNumber());
        invoice.setReadyForApproval(record.getReadyForApproval() != null && record.getReadyForApproval());
        invoice.setApprovalComment(record.getApprovalComment());
        invoice.setState(record.getStatus());
        return invoice;
    }

    private void fromInvoice(InvoiceRecord record, Invoice invoice) {
        record.setEmployeeId(invoice.getEmployeeId());
        record.setAmount(invoice.getAmount());
        record.setVat(invoice.getVat());
        record.setDueDate(invoice.getDueDate());
        record.setSubmittedAt(invoice.getSubmittedAt());
        record.setSubmitterId(invoice.getSubmitterId());
        record.setSubmitterDeviceId(invoice.getSubmitterDeviceId());
        record.setApprovalDate(invoice.getApprovalDate());
        record.setApproverId(invoice.getApproverId());
        record.setRejectionReason(invoice.getRejectionReason());
        record.setRejectedBy(invoice.getRejectedBy());
        record.setRejectionDate(invoice.getRejectionDate());
        record.setReceiptNumber(invoice.getReceiptNumber());
        record.setReadyForApproval(invoice.isReadyForApproval());
        record.setApprovalComment(invoice.getApprovalComment());
        record.setStatus(invoice.getState() != null ? invoice.getState() : InvoiceState.DRAFT);
    }

    private LocalDate asLocalDate(Object date) {
        return switch (date) {
            case null -> null;
            case LocalDate ld -> ld;
            case java.sql.Date d -> d.toLocalDate();
            case java.time.chrono.ChronoLocalDate cld -> LocalDate.from(cld);
            default -> throw new IllegalArgumentException("Unknown date type: " + date.getClass());
        };
    }
}
