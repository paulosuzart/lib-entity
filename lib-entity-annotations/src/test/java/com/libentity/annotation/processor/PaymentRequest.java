package com.libentity.annotation.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRequest {
    private final int amount;
    // add more fields as needed
}
