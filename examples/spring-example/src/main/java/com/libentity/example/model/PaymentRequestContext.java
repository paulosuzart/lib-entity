package com.libentity.example.model;

public record PaymentRequestContext(PaymentAggregate payment, PaymentAggregate newPayment) {}
