package com.libentity.example.payment.command;

import java.util.UUID;

public record ApprovePaymentCommand(String comment, UUID approverId) {}
