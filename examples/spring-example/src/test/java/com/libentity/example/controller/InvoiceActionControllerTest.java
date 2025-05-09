package com.libentity.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.libentity.example.ExampleApplication;
import com.libentity.example.commands.CreateInvoiceCommand;
import com.libentity.example.model.InvoiceFilter;
import com.libentity.example.service.InvoiceWithRateResponse;
import com.libentity.example.test.BaseIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(classes = ExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InvoiceActionControllerTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAndFindInvoice() {
        // Create invoice
        CreateInvoiceCommand cmd = new CreateInvoiceCommand();
        cmd.setEmployeeId("emp1");
        cmd.setVat(BigDecimal.valueOf(10));
        cmd.setAmount(BigDecimal.valueOf(100));
        cmd.setDueDate(LocalDate.now().plusDays(10));
        cmd.setSubmitterId("user1");
        cmd.setSubmitterDeviceId("device1");

        ResponseEntity<InvoiceWithRateResponse> createResp =
                restTemplate.postForEntity("/invoice/action/create", cmd, InvoiceWithRateResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(createResp.getBody()).getInvoice()).isNotNull();
        Long createdId =
                Objects.requireNonNull(createResp.getBody().getInvoice().getId());

        // Find invoice by filter (assumes filter by employeeId works)
        InvoiceFilter filter = new InvoiceFilter();
        filter.setLimit(10);
        filter.setOffset(0);
        // set employeeIdIn if available in InvoiceFilter
        // filter.setEmployeeIdIn(Set.of("emp1"));

        ResponseEntity<InvoiceWithRateResponse[]> filterResp =
                restTemplate.postForEntity("/invoice/action/filter", filter, InvoiceWithRateResponse[].class);
        assertThat(filterResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        InvoiceWithRateResponse[] body = filterResp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.length).isGreaterThanOrEqualTo(1);
        boolean found = false;
        for (InvoiceWithRateResponse resp : body) {
            if (resp.getInvoice() != null && resp.getInvoice().getId().equals(createdId)) {
                assertThat(resp.getInvoice().getEmployeeId()).isEqualTo("emp1");
                assertThat(resp.getInvoice().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
                assertThat(resp.getInvoice().getVat()).isEqualByComparingTo(BigDecimal.valueOf(10));
                assertThat(resp.getInvoice().getDueDate()).isEqualTo(cmd.getDueDate());
                // Assert allowed actions is not null and matches expected for DRAFT
                assertThat(resp.getAllowedActions()).isNotNull();
                // Based on InvoiceEntityTypeConfig, DRAFT state allows only 'submit' (if amount is set and valid)
                assertThat(resp.getAllowedActions()).containsExactly("submit");
                found = true;
            }
        }
        assertThat(found)
                .as("Created invoice should be present in filter results")
                .isTrue();
    }
}
