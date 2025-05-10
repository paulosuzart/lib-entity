package com.libentity.example.invoice.service;

import com.libentity.example.invoice.model.Invoice;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class InvoiceWithRateResponse {
    private Invoice invoice;
    private BigDecimal exchangeRate;
    private List<String> allowedActions;
}
