package com.libentity.example.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.libentity.core.filter.RangeFilter;
import com.libentity.example.ExampleApplication;
import com.libentity.example.invoice.model.Invoice;
import com.libentity.example.invoice.model.InvoiceFilter;
import com.libentity.example.invoice.model.InvoiceState;
import com.libentity.example.invoice.repository.InvoiceRepository;
import com.libentity.example.test.BaseIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoiceRepositoryIntegrationTest extends BaseIntegrationTest {
    @Autowired
    InvoiceRepository invoiceRepository;

    @Test
    void canPersistAndLoadInvoice() {
        Invoice invoice = new Invoice();
        invoice.setEmployeeId("emp42");
        invoice.setAmount(new BigDecimal("123.45"));
        invoice.setVat(new BigDecimal("12.34"));
        invoice.setDueDate(LocalDate.now().plusDays(10));
        invoice.setSubmittedAt(LocalDate.now());
        invoice.setSubmitterId("user42");
        invoice.setSubmitterDeviceId("device42");
        invoice.setApprovalDate(LocalDate.now().plusDays(2));
        invoice.setApproverId("approver42");
        invoice.setRejectionReason(null);
        invoice.setRejectedBy(null);
        invoice.setRejectionDate(null);
        invoice.setReceiptNumber("RCPT-42");
        invoice.setReadyForApproval(true);
        invoice.setApprovalComment("All good");
        invoice.setState(InvoiceState.DRAFT);

        // Save
        invoiceRepository.save(invoice);

        // Fetch the invoice by employeeId (unique in this test)
        Invoice loaded = invoiceRepository.findByFilter(new InvoiceFilter()).stream()
                .filter(inv -> "emp42".equals(inv.getEmployeeId()))
                .findFirst()
                .orElse(null);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getEmployeeId()).isEqualTo("emp42");
        assertThat(loaded.getAmount()).isEqualByComparingTo("123.45");
        assertThat(loaded.getVat()).isEqualByComparingTo("12.34");
        assertThat(loaded.getDueDate()).isEqualTo(invoice.getDueDate());
        assertThat(loaded.isReadyForApproval()).isTrue();
        assertThat(loaded.getState()).isEqualTo(InvoiceState.DRAFT);
    }

    @Test
    void canFilterInvoices() {
        Invoice invoice1 = new Invoice();
        invoice1.setEmployeeId("empA");
        invoice1.setAmount(new BigDecimal("100.00"));
        invoice1.setVat(new BigDecimal("10.00"));
        invoice1.setDueDate(LocalDate.now().plusDays(5));
        invoice1.setReadyForApproval(true);
        invoice1.setState(InvoiceState.DRAFT);
        invoiceRepository.save(invoice1);

        Invoice invoice2 = new Invoice();
        invoice2.setEmployeeId("empB");
        invoice2.setAmount(new BigDecimal("200.00"));
        invoice2.setVat(new BigDecimal("20.00"));
        invoice2.setDueDate(LocalDate.now().plusDays(10));
        invoice2.setReadyForApproval(false);
        invoice2.setState(InvoiceState.DRAFT);
        invoiceRepository.save(invoice2);

        // Filter: amount > 150
        var amountFilter = new InvoiceFilter();
        var range = new RangeFilter<BigDecimal>();
        range.setGt(new BigDecimal("150.00"));
        amountFilter.setAmount(range);
        var result = invoiceRepository.findByFilter(amountFilter);
        assertThat(result).extracting(Invoice::getEmployeeId).containsExactly("empB");

        // Filter: readyForApproval true
        var readyFilter = new InvoiceFilter();
        readyFilter.setReadyForApproval(true);
        var readyResult = invoiceRepository.findByFilter(readyFilter);
        assertThat(readyResult).extracting(Invoice::getEmployeeId).contains("empA");

        // Filter: dueDate < now + 7
        var dueDateFilter = new InvoiceFilter();
        var dueRange = new RangeFilter<java.time.chrono.ChronoLocalDate>();
        dueRange.setLt(LocalDate.now().plusDays(7));
        dueDateFilter.setDueDate(dueRange);
        var dueResult = invoiceRepository.findByFilter(dueDateFilter);
        assertThat(dueResult).extracting(Invoice::getEmployeeId).contains("empA");

        // Filter: employeeId in [empA, empB]
        var inFilter = new InvoiceFilter();
        inFilter.setEmployeeIdIn(Set.of("empA", "empB"));
        var inResult = invoiceRepository.findByFilter(inFilter);
        assertThat(inResult).extracting(Invoice::getEmployeeId).containsExactlyInAnyOrder("empA", "empB");
    }
}
