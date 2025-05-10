package com.libentity.example.payment.model;

public record PaymentRequestContext(PaymentAggregate payment, PaymentAggregate newPayment) {}
