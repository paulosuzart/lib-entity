package com.libentity.core;

import com.libentity.core.action.ActionCommand;
import com.libentity.core.entity.EntityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import lombok.Data;

public class InvoiceExample {
    enum InvoiceState {
        DRAFT,
        PENDING,
        APPROVED,
        REJECTED
    }

    @Data
    static class Invoice {
        private BigDecimal amount;
        private BigDecimal vat;
        private boolean readyForApproval;
        private String submitterId;
        private String submitterDeviceId;
        private String approverId;
        private String approvalComment;
        private String rejectedBy;
        private String rejectionReason;
        private LocalDate rejectionDate;
    }

    @Data
    public static class InvoiceRequestContext {
        private Invoice invoice;
        private String userId;
        private String deviceId;
        private long timestamp;
    }

    @Data
    static class SubmitInvoiceCommand implements ActionCommand {
        private String submitterId;
        private String submitterDeviceId;

        @Override
        public String getActionName() {
            return "submit";
        }
    }

    @Data
    static class ApproveInvoiceCommand implements ActionCommand {
        private String approverId;
        private String approvalComment;

        @Override
        public String getActionName() {
            return "approve";
        }
    }

    @Data
    static class RejectInvoiceCommand implements ActionCommand {
        private String rejectorId;
        private String rejectionReason;

        @Override
        public String getActionName() {
            return "reject";
        }
    }

    @Data
    static class SetVatCommand implements ActionCommand {
        private BigDecimal vat;

        @Override
        public String getActionName() {
            return "setVat";
        }
    }

    @Data
    static class ClearVatCommand implements ActionCommand {
        @Override
        public String getActionName() {
            return "clearVat";
        }
    }

    public static EntityType<InvoiceState, InvoiceRequestContext> defineInvoice() {
        return EntityType.<InvoiceState, InvoiceRequestContext>builder("Invoice")
                // Define fields with their validation rules
                .field("amount", BigDecimal.class, f -> f.validateInState(InvoiceState.DRAFT, (state, request, ctx) -> {
                            // Only type/format validation here; business rule in onlyIf!
                            if (request.getInvoice().getAmount() != null
                                    && request.getInvoice().getAmount().compareTo(new BigDecimal("10000")) > 0) {
                                ctx.addError("AMOUNT_TOO_LARGE", "Amount cannot exceed 10,000");
                            }
                        })
                        .validateStateTransition(
                                InvoiceState.DRAFT, InvoiceState.PENDING, (fromState, toState, request, ctx) -> {
                                    if (request.getInvoice().getAmount() == null) {
                                        ctx.addError("AMOUNT_REQUIRED", "Amount must be set");
                                    }
                                    if (request.getInvoice().getAmount() != null
                                            && request.getInvoice().getAmount().compareTo(new BigDecimal("1000")) > 0) {
                                        ctx.addError(
                                                "AMOUNT_APPROVAL_LIMIT", "Amount exceeds manager approval threshold");
                                    }
                                }))
                .field("vat", BigDecimal.class, f -> f.validateInState(InvoiceState.DRAFT, (state, request, ctx) -> {
                            if (request.getInvoice().getVat() == null
                                    || request.getInvoice().getVat().compareTo(BigDecimal.ZERO) < 0) {
                                ctx.addError("VAT_INVALID", "VAT cannot be negative");
                            }
                            if (request.getInvoice().getVat() != null
                                    && request.getInvoice().getAmount() != null) {
                                BigDecimal maxVat =
                                        request.getInvoice().getAmount().multiply(new BigDecimal("0.25"));
                                if (request.getInvoice().getVat().compareTo(maxVat) > 0) {
                                    ctx.addError("VAT_TOO_HIGH", "VAT cannot exceed 25% of amount");
                                }
                            }
                        })
                        .validateInState(InvoiceState.PENDING, (state, request, ctx) -> {
                            if (request.getInvoice().getVat() != null
                                    && request.getInvoice().getAmount() != null
                                    && request.getInvoice()
                                                    .getVat()
                                                    .compareTo(
                                                            request.getInvoice().getAmount())
                                            > 0) {
                                ctx.addError("VAT_TOO_HIGH_PENDING", "VAT cannot be greater than amount");
                            }
                        }))
                // Add state validators
                .validateInState(InvoiceState.PENDING, (state, request, ctx) -> {
                    if (!request.getInvoice().isReadyForApproval()) {
                        ctx.addError("NOT_READY", "Invoice is not ready for approval");
                    }
                })
                .validateInState(InvoiceState.APPROVED, (state, request, ctx) -> {
                    if (request.getInvoice().getApproverId() == null
                            || request.getInvoice().getApproverId().isEmpty()) {
                        ctx.addError("APPROVER_ID_REQUIRED", "Approver ID must be set");
                    }
                    if (request.getInvoice().getApprovalComment() == null
                            || request.getInvoice().getApprovalComment().isEmpty()) {
                        ctx.addError("APPROVAL_COMMENT_REQUIRED", "Approval comment is required");
                    }
                })
                .validateInState(InvoiceState.REJECTED, (state, request, ctx) -> {
                    if (request.getInvoice().getRejectedBy() == null
                            || request.getInvoice().getRejectedBy().isEmpty()) {
                        ctx.addError("REJECTED_BY_REQUIRED", "Rejected by must be set");
                    }
                    if (request.getInvoice().getRejectionReason() == null
                            || request.getInvoice().getRejectionReason().isEmpty()) {
                        ctx.addError("REJECTION_REASON_REQUIRED", "Rejection reason is required");
                    }
                    if (request.getInvoice().getRejectionDate() == null) {
                        ctx.addError("REJECTION_DATE_REQUIRED", "Rejection date must be set");
                    }
                })
                // Add transition validators
                .validateTransition(InvoiceState.PENDING, InvoiceState.APPROVED, (fromState, toState, request, ctx) -> {
                    if (request.getUserId() == null || request.getUserId().isEmpty()) {
                        ctx.addError("APPROVER_REQUIRED", "Approver userId is required");
                    }
                    if (!request.getUserId().startsWith("mgr_")
                            && request.getInvoice().getAmount() != null
                            && request.getInvoice().getAmount().compareTo(new BigDecimal("500")) > 0) {
                        ctx.addError("APPROVER_ROLE", "Only managers can approve invoices over 500");
                    }
                })
                .validateTransition(InvoiceState.PENDING, InvoiceState.REJECTED, (fromState, toState, request, ctx) -> {
                    if (request.getUserId() == null || request.getUserId().isEmpty()) {
                        ctx.addError("REJECTOR_REQUIRED", "Rejector userId is required");
                    }
                })
                // Define actions
                .<SubmitInvoiceCommand>action("submit", a -> a.allowedStates(Set.of(InvoiceState.DRAFT))
                        .onlyIf((state, request, command) ->
                                request.getInvoice().getAmount() != null
                                        && request.getInvoice().getAmount().compareTo(BigDecimal.ZERO) > 0
                                        && request.getInvoice().getAmount().compareTo(new BigDecimal("10000")) <= 0)
                        .handler((state, request, command, mutator) -> {
                            request.getInvoice().setReadyForApproval(true);
                            request.getInvoice().setSubmitterId(command.getSubmitterId());
                            request.getInvoice().setSubmitterDeviceId(command.getSubmitterDeviceId());
                            mutator.setState(InvoiceState.PENDING);
                        }))
                .<ApproveInvoiceCommand>action("approve", a -> a.allowedStates(Set.of(InvoiceState.PENDING))
                        .onlyIf((state, request, command) ->
                                request.getInvoice().isReadyForApproval())
                        .handler((state, request, command, mutator) -> {
                            request.getInvoice().setApproverId(command.getApproverId());
                            request.getInvoice().setApprovalComment(command.getApprovalComment());
                            mutator.setState(InvoiceState.APPROVED);
                        }))
                .<RejectInvoiceCommand>action("reject", a -> a.allowedStates(Set.of(InvoiceState.PENDING))
                        .onlyIf((state, request, command) -> command.getRejectionReason() != null
                                && !command.getRejectionReason().isEmpty())
                        .handler((state, request, command, mutator) -> {
                            request.getInvoice().setRejectedBy(command.getRejectorId());
                            request.getInvoice().setRejectionReason(command.getRejectionReason());
                            request.getInvoice().setRejectionDate(LocalDate.now());
                            request.getInvoice().setApproverId(command.getRejectorId());
                            request.getInvoice().setApprovalComment(command.getRejectionReason());
                            mutator.setState(InvoiceState.REJECTED);
                        }))
                .<SetVatCommand>action("setVat", a -> a.allowedStates(Set.of(InvoiceState.DRAFT, InvoiceState.PENDING))
                        .onlyIf((state, request, command) -> true)
                        .handler((state, request, command, mutator) -> {
                            request.getInvoice().setVat(command.getVat());
                        }))
                .<ClearVatCommand>action(
                        "clearVat", a -> a.allowedStates(Set.of(InvoiceState.DRAFT, InvoiceState.PENDING))
                                .onlyIf((state, request, command) -> true)
                                .handler((state, request, command, mutator) -> {
                                    request.getInvoice().setVat(BigDecimal.ZERO);
                                }))
                .build();
    }
}
