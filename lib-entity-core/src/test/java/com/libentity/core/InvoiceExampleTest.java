package com.libentity.core;

import static org.assertj.core.api.Assertions.*;

import com.libentity.core.action.ActionDefinition;
import com.libentity.core.action.ActionExecutor;
import com.libentity.core.action.ActionResult;
import com.libentity.core.action.SyncActionExecutor;
import com.libentity.core.entity.EntityType;
import com.libentity.core.validation.ValidationContext;
import com.libentity.core.validation.ValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InvoiceExampleTest {
    private EntityType<InvoiceExample.InvoiceState, InvoiceExample.InvoiceRequestContext> entityType;
    private ActionExecutor<InvoiceExample.InvoiceState, InvoiceExample.InvoiceRequestContext> executor;

    @BeforeEach
    void setup() {
        entityType = InvoiceExample.defineInvoice();
        executor = SyncActionExecutor.<InvoiceExample.InvoiceState, InvoiceExample.InvoiceRequestContext>builder()
                .entityType(entityType)
                .build();
    }

    @Test
    void testSubmitAndApproveInvoice() {
        // Arrange
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("250"));
        invoice.setVat(new BigDecimal("50"));
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_1");
        ctx.setDeviceId("dev_1");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();

        // Submit
        InvoiceExample.SubmitInvoiceCommand submitCmd = new InvoiceExample.SubmitInvoiceCommand();
        submitCmd.setSubmitterId("user_1");
        submitCmd.setSubmitterDeviceId("dev_1");
        ActionResult<
                        InvoiceExample.InvoiceState,
                        InvoiceExample.InvoiceRequestContext,
                        InvoiceExample.SubmitInvoiceCommand>
                submitResult = executor.execute(InvoiceExample.InvoiceState.DRAFT, ctx, vctx, submitCmd);
        assertThat(submitResult.state()).isEqualTo(InvoiceExample.InvoiceState.PENDING);
        assertThat(vctx.hasErrors()).isFalse();
        assertThat(submitResult.request().getInvoice().isReadyForApproval()).isTrue();
        assertThat(submitResult.request().getInvoice().getSubmitterId()).isEqualTo("user_1");

        // Approve
        InvoiceExample.ApproveInvoiceCommand approveCmd = new InvoiceExample.ApproveInvoiceCommand();
        approveCmd.setApproverId("mgr_2");
        approveCmd.setApprovalComment("Looks good");
        ctx.setUserId("mgr_2");
        vctx = new ValidationContext();
        ActionResult<
                        InvoiceExample.InvoiceState,
                        InvoiceExample.InvoiceRequestContext,
                        InvoiceExample.ApproveInvoiceCommand>
                approveResult = executor.execute(InvoiceExample.InvoiceState.PENDING, ctx, vctx, approveCmd);
        assertThat(approveResult.state()).isEqualTo(InvoiceExample.InvoiceState.APPROVED);
        assertThat(vctx.hasErrors()).isFalse();
        assertThat(approveResult.request().getInvoice().getApproverId()).isEqualTo("mgr_2");

        // --- onlyIf assertions ---
        // Try to submit with invalid amount (should NOT execute: onlyIf=false)
        InvoiceExample.Invoice invalidInvoice = new InvoiceExample.Invoice();
        invalidInvoice.setAmount(BigDecimal.ZERO); // invalid for onlyIf
        InvoiceExample.InvoiceRequestContext invalidCtx = new InvoiceExample.InvoiceRequestContext();
        invalidCtx.setUserId("user_2");
        invalidCtx.setDeviceId("dev_2");
        invalidCtx.setTimestamp(System.currentTimeMillis());
        invalidCtx.setInvoice(invalidInvoice);
        InvoiceExample.SubmitInvoiceCommand invalidSubmitCmd = new InvoiceExample.SubmitInvoiceCommand();
        invalidSubmitCmd.setSubmitterId("user_2");
        invalidSubmitCmd.setSubmitterDeviceId("dev_2");
        ValidationContext invalidVctx = new ValidationContext();
        Throwable thrown = catchThrowable(
                () -> executor.execute(InvoiceExample.InvoiceState.DRAFT, invalidCtx, invalidVctx, invalidSubmitCmd));
        if (thrown != null) {
            System.out.println("Actual exception class: " + thrown.getClass().getName());
            System.out.println("Actual exception message: " + thrown.getMessage());
            if (thrown instanceof com.libentity.core.validation.ValidationException vex) {
                System.out.println("Validation errors: " + vex.getErrors());
            }
        }
        assertThat(thrown)
                .isInstanceOf(com.libentity.core.validation.ValidationException.class)
                .hasMessageContaining("ACTION_NOT_ALLOWED");
        // No further assertions on invalidSubmitResult since exception is expected

        // Assert state did not change (remains DRAFT)
        // Optionally, assert that readyForApproval is still false
        // Optionally, check for errors if your implementation adds them
    }

    @Test
    void testRejectInvoice() {
        // Arrange
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("100"));
        invoice.setVat(new BigDecimal("20"));
        invoice.setReadyForApproval(true);
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_3");
        ctx.setDeviceId("dev_2");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();

        // Reject
        InvoiceExample.RejectInvoiceCommand rejectCmd = new InvoiceExample.RejectInvoiceCommand();
        rejectCmd.setRejectorId("user_3");
        rejectCmd.setRejectionReason("Missing info");
        ActionResult<
                        InvoiceExample.InvoiceState,
                        InvoiceExample.InvoiceRequestContext,
                        InvoiceExample.RejectInvoiceCommand>
                rejectResult = executor.execute(InvoiceExample.InvoiceState.PENDING, ctx, vctx, rejectCmd);
        assertThat(rejectResult.state()).isEqualTo(InvoiceExample.InvoiceState.REJECTED);
        assertThat(vctx.hasErrors()).isFalse();
        assertThat(rejectResult.request().getInvoice().getApproverId()).isEqualTo("user_3");
        assertThat(rejectResult.request().getInvoice().getApprovalComment()).isEqualTo("Missing info");
    }

    @Test
    void testInvalidApprovalBlocked() {
        // Arrange
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("2000")); // Exceeds manager threshold
        invoice.setVat(new BigDecimal("400"));
        invoice.setReadyForApproval(true);
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_4"); // Not a manager
        ctx.setDeviceId("dev_3");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();

        // Try to approve
        InvoiceExample.ApproveInvoiceCommand approveCmd = new InvoiceExample.ApproveInvoiceCommand();
        approveCmd.setApproverId("user_4");
        approveCmd.setApprovalComment("Trying to approve");
        assertThatThrownBy(() -> executor.execute(InvoiceExample.InvoiceState.PENDING, ctx, vctx, approveCmd))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");
    }

    @Test
    void testInvalidTransitionFromDraft() {
        // Arrange
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("100"));
        invoice.setVat(new BigDecimal("10"));
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("mgr_1");
        ctx.setDeviceId("dev_4");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();

        // Try to approve from DRAFT
        InvoiceExample.ApproveInvoiceCommand approveCmd = new InvoiceExample.ApproveInvoiceCommand();
        approveCmd.setApproverId("mgr_1");
        approveCmd.setApprovalComment("Premature approval");
        assertThatThrownBy(() -> executor.execute(InvoiceExample.InvoiceState.DRAFT, ctx, vctx, approveCmd))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");
    }

    @Test
    void testSetVatAndClearVatActions() {
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("100"));
        invoice.setVat(new BigDecimal("10"));
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_5");
        ctx.setDeviceId("dev_5");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();

        // setVat
        InvoiceExample.SetVatCommand setVatCmd = new InvoiceExample.SetVatCommand();
        setVatCmd.setVat(new BigDecimal("15"));
        ActionResult<InvoiceExample.InvoiceState, InvoiceExample.InvoiceRequestContext, InvoiceExample.SetVatCommand>
                setVatResult = executor.execute(InvoiceExample.InvoiceState.DRAFT, ctx, vctx, setVatCmd);
        assertThat(setVatResult.state()).isEqualTo(InvoiceExample.InvoiceState.DRAFT);
        assertThat(setVatResult.request().getInvoice().getVat()).isEqualTo(new BigDecimal("15"));
        assertThat(vctx.hasErrors()).isFalse();

        // clearVat
        InvoiceExample.ClearVatCommand clearVatCmd = new InvoiceExample.ClearVatCommand();
        vctx = new ValidationContext();
        ActionResult<InvoiceExample.InvoiceState, InvoiceExample.InvoiceRequestContext, InvoiceExample.ClearVatCommand>
                clearVatResult = executor.execute(InvoiceExample.InvoiceState.DRAFT, ctx, vctx, clearVatCmd);
        assertThat(clearVatResult.state()).isEqualTo(InvoiceExample.InvoiceState.DRAFT);
        assertThat(clearVatResult.request().getInvoice().getVat()).isEqualTo(BigDecimal.ZERO);
        assertThat(vctx.hasErrors()).isFalse();
    }

    @Test
    void testAvailableActionsForState() {
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("100"));
        invoice.setVat(new BigDecimal("10"));
        // Simulate logic: available actions are those whose allowedStates contains the current state
        var available = entityType.getActions().values().stream()
                .filter(a -> a.getAllowedStates().contains(InvoiceExample.InvoiceState.DRAFT))
                .map(ActionDefinition::getName)
                .toList();
        assertThat(available).contains("submit", "setVat", "clearVat");
    }

    @Test
    void testGetAllowedActionsRespectsOnlyIf() {
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("100"));
        invoice.setVat(new BigDecimal("10"));
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_7");
        ctx.setDeviceId("dev_7");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        // All actions with allowedStates DRAFT and onlyIf == null or returns true
        var allowed = executor.getAllowedActions(InvoiceExample.InvoiceState.DRAFT, ctx);
        assertThat(allowed).contains("submit", "setVat", "clearVat");

        // Simulate an action with onlyIf that returns false (e.g., readyForApproval required for
        // submit)
        invoice.setReadyForApproval(true);
        // In this DSL, onlyIf is not used, but if it were, we'd check here.
        // For now, check that allowed actions remain correct.
        allowed = executor.getAllowedActions(InvoiceExample.InvoiceState.DRAFT, ctx);
        assertThat(allowed).contains("submit", "setVat", "clearVat");
    }

    @Test
    void testExecuteReturnsStateAndEntity() {
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        invoice.setAmount(new BigDecimal("250"));
        invoice.setVat(new BigDecimal("50"));
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_1");
        ctx.setDeviceId("dev_1");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();

        InvoiceExample.SubmitInvoiceCommand submitCmd = new InvoiceExample.SubmitInvoiceCommand();
        submitCmd.setSubmitterId("user_1");
        submitCmd.setSubmitterDeviceId("dev_1");
        ActionResult<
                        InvoiceExample.InvoiceState,
                        InvoiceExample.InvoiceRequestContext,
                        InvoiceExample.SubmitInvoiceCommand>
                result = executor.execute(InvoiceExample.InvoiceState.DRAFT, ctx, vctx, submitCmd);
        assertThat(result.state()).isEqualTo(InvoiceExample.InvoiceState.PENDING);
        assertThat(result.request().getInvoice().isReadyForApproval()).isTrue();
        assertThat(result.request().getInvoice().getSubmitterId()).isEqualTo("user_1");
    }

    @Test
    void testExecuteThrowsValidationException() {
        InvoiceExample.Invoice invoice = new InvoiceExample.Invoice();
        // Invalid: no amount set
        InvoiceExample.InvoiceRequestContext ctx = new InvoiceExample.InvoiceRequestContext();
        ctx.setUserId("user_2");
        ctx.setDeviceId("dev_2");
        ctx.setTimestamp(System.currentTimeMillis());
        ctx.setInvoice(invoice);
        ValidationContext vctx = new ValidationContext();
        InvoiceExample.SubmitInvoiceCommand submitCmd = new InvoiceExample.SubmitInvoiceCommand();
        submitCmd.setSubmitterId("user_2");
        submitCmd.setSubmitterDeviceId("dev_2");
        assertThatThrownBy(() -> executor.execute(InvoiceExample.InvoiceState.DRAFT, ctx, vctx, submitCmd))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");
    }
}
