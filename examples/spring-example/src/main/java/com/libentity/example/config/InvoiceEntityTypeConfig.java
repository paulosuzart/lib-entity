package com.libentity.example.config;

import com.libentity.core.action.ActionExecutor;
import com.libentity.core.action.SyncActionExecutor;
import com.libentity.core.entity.EntityType;
import com.libentity.example.commands.*;
import com.libentity.example.model.InvoiceRequestContext;
import com.libentity.example.model.InvoiceState;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceEntityTypeConfig {

    @Bean
    public EntityType<InvoiceState, InvoiceRequestContext> invoiceEntityType() {
        EntityType<InvoiceState, InvoiceRequestContext> entityType =
                EntityType.<InvoiceState, InvoiceRequestContext>builder("Invoice")
                        .field("amount", BigDecimal.class, f -> f.validateInState(
                                        InvoiceState.DRAFT, (state, request, ctx) -> {
                                            if (request.newInvoice().getAmount() == null
                                                    || request.newInvoice()
                                                                    .getAmount()
                                                                    .compareTo(BigDecimal.ZERO)
                                                            <= 0) {
                                                ctx.addError("AMOUNT_INVALID", "Amount must be positive");
                                            }
                                            if (request.newInvoice().getAmount() != null
                                                    && request.newInvoice()
                                                                    .getAmount()
                                                                    .compareTo(new BigDecimal("10000"))
                                                            > 0) {
                                                ctx.addError("AMOUNT_TOO_LARGE", "Amount cannot exceed 10,000");
                                            }
                                        })
                                .validateStateTransition(
                                        InvoiceState.DRAFT,
                                        InvoiceState.PENDING_APPROVAL,
                                        (fromState, toState, request, ctx) -> {
                                            if (request.newInvoice().getAmount() == null
                                                    || request.newInvoice()
                                                                    .getAmount()
                                                                    .compareTo(new BigDecimal("1000"))
                                                            > 0) {
                                                ctx.addError(
                                                        "AMOUNT_APPROVAL_LIMIT",
                                                        "Amount exceeds manager approval threshold");
                                            }
                                        }))
                        .field(
                                "vat",
                                BigDecimal.class,
                                f -> f.validateInState(InvoiceState.DRAFT, (state, request, ctx) -> {
                                    if (request.newInvoice().getVat() == null) {
                                        ctx.addError("VAT_REQUIRED", "VAT is required");
                                    }
                                }))
                        .field(
                                "dueDate",
                                LocalDate.class,
                                f -> f.validateInState(InvoiceState.DRAFT, (state, request, ctx) -> {
                                    if (request.newInvoice().getDueDate() == null) {
                                        ctx.addError("DUE_DATE_REQUIRED", "Due date is required");
                                    } else if (!request.newInvoice()
                                            .getDueDate()
                                            .isAfter(LocalDate.now())) {
                                        ctx.addError("DUE_DATE_PAST", "Due date must be in the future");
                                    } else if (request.newInvoice()
                                            .getDueDate()
                                            .isAfter(LocalDate.now().plusYears(1))) {
                                        ctx.addError(
                                                "DUE_DATE_TOO_FAR",
                                                "Due date cannot be more than 1 year in the future");
                                    }
                                }))
                        .field(
                                "readyForApproval",
                                Boolean.class,
                                f -> f.validateInState(InvoiceState.DRAFT, (state, request, ctx) -> {
                                    if (Boolean.TRUE.equals(request.newInvoice().isReadyForApproval())) {
                                        if (request.newInvoice().getAmount() == null
                                                || request.newInvoice()
                                                                .getAmount()
                                                                .compareTo(BigDecimal.ZERO)
                                                        <= 0) {
                                            ctx.addError(
                                                    "READY_FOR_APPROVAL_INVALID",
                                                    "Amount must be set and positive before approval");
                                        }
                                        if (request.newInvoice().getDueDate() == null
                                                || !request.newInvoice()
                                                        .getDueDate()
                                                        .isAfter(LocalDate.now())) {
                                            ctx.addError(
                                                    "READY_FOR_APPROVAL_INVALID",
                                                    "Due date must be set and in the future before approval");
                                        }
                                    }
                                }))
                        // Define actions with onlyIf predicates
                        .<CreateInvoiceCommand>action("create", a -> a.onlyIf((state, request, command) ->
                                        state == null && request.newInvoice().getId() == null)
                                .handler((state, request, command, mutator) -> {
                                    // Set state to DRAFT
                                    request.newInvoice().setState(InvoiceState.DRAFT);
                                    // Set fields from command
                                    request.newInvoice().setEmployeeId(command.getEmployeeId());
                                    request.newInvoice().setVat(command.getVat());
                                    request.newInvoice().setAmount(command.getAmount());
                                    request.newInvoice().setDueDate(command.getDueDate());
                                    request.newInvoice().setSubmitterId(command.getSubmitterId());
                                    request.newInvoice().setSubmitterDeviceId(command.getSubmitterDeviceId());
                                    mutator.setState(InvoiceState.DRAFT);
                                }))
                        .<SubmitInvoiceCommand>action("submit", a -> a.allowedStates(Set.of(InvoiceState.DRAFT))
                                .onlyIf((state, request, command) -> request.newInvoice()
                                                        .getAmount()
                                                != null
                                        && request.newInvoice().getAmount().compareTo(BigDecimal.ZERO) > 0
                                        && request.newInvoice().getAmount().compareTo(new BigDecimal("10000")) <= 0)
                                .handler((state, request, command, mutator) -> {
                                    request.newInvoice().setReadyForApproval(true);
                                    request.newInvoice().setSubmitterId(command.getSubmitterId());
                                    request.newInvoice().setSubmitterDeviceId(command.getSubmitterDeviceId());
                                    mutator.setState(InvoiceState.PENDING_APPROVAL);
                                }))
                        .<ApproveInvoiceCommand>action(
                                "approve", a -> a.allowedStates(Set.of(InvoiceState.PENDING_APPROVAL))
                                        .onlyIf((state, request, command) ->
                                                request.newInvoice().isReadyForApproval())
                                        .handler((state, request, command, mutator) -> {
                                            request.newInvoice().setApprovalDate(LocalDate.now());
                                            request.newInvoice().setApproverId(command.getApproverId());
                                            request.newInvoice().setApprovalComment(command.getApprovalComment());
                                            mutator.setState(InvoiceState.APPROVED);
                                        }))
                        .<RejectInvoiceCommand>action("reject", a -> a.allowedStates(
                                        Set.of(InvoiceState.PENDING_APPROVAL))
                                .onlyIf((state, request, command) -> command.getRejectionReason() != null
                                        && !command.getRejectionReason().isEmpty())
                                .handler((state, request, command, mutator) -> {
                                    request.newInvoice().setRejectionReason(command.getRejectionReason());
                                    // request is InvoiceRequestContext, which is a record with a single Invoice field
                                    // Use entityData.getSubmitterId() as fallback for rejectedBy if user id is not
                                    // available
                                    String rejectedBy = request.newInvoice().getSubmitterId();
                                    request.newInvoice().setRejectedBy(rejectedBy);
                                    request.newInvoice().setRejectionDate(LocalDate.now());
                                    mutator.setState(InvoiceState.REJECTED);
                                }))
                        .<MarkAsPaidCommand>action("markAsPaid", a -> a.allowedStates(Set.of(InvoiceState.APPROVED))
                                .onlyIf((state, request, command) ->
                                        request.newInvoice().getReceiptNumber() == null
                                                || request.newInvoice()
                                                        .getReceiptNumber()
                                                        .isEmpty())
                                .handler((state, request, command, mutator) -> {
                                    request.newInvoice().setReceiptNumber(command.getReceiptNumber());
                                    mutator.setState(InvoiceState.PAID);
                                }))
                        .build();

        return entityType;
    }

    @Bean
    public ActionExecutor<InvoiceState, InvoiceRequestContext> actionExecutor(
            @Qualifier("invoiceEntityType") EntityType<InvoiceState, InvoiceRequestContext> invoiceEntityType) {
        return SyncActionExecutor.<InvoiceState, InvoiceRequestContext>builder()
                .entityType(invoiceEntityType)
                .build();
    }
}
