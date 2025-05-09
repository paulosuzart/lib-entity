package com.libentity.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.libentity.example.ExampleApplication;
import com.libentity.example.commands.ApprovePaymentCommand;
import com.libentity.example.model.PaymentAggregate;
import com.libentity.example.model.PaymentState;
import com.libentity.example.test.BaseIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentServiceTest extends BaseIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void approvePayment_shouldSetStateToApprovedAndSetComment() {
        // Arrange
        String expectedComment = "Payment approved by test";
        UUID approverId = UUID.randomUUID();
        ApprovePaymentCommand command = new ApprovePaymentCommand(expectedComment, approverId);

        // Act
        PaymentAggregate result = paymentService.approvePayment(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getState()).isEqualTo(PaymentState.APPROVED);
        assertThat(result.getApprovalComment()).isEqualTo(expectedComment);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getAmount()).isNotNull();
    }
}
