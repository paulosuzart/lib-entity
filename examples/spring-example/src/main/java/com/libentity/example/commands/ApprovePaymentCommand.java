package com.libentity.example.commands;

import java.util.UUID;

public record ApprovePaymentCommand(String comment, UUID approverId) {}
