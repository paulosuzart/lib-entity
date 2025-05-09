package com.libentity.annotation.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmitPaymentCommand {
    private final String submitDate;
    private final String submitterId;
}
