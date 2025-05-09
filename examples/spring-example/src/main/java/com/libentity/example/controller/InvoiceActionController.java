package com.libentity.example.controller;

import com.libentity.example.commands.CreateInvoiceCommand;
import com.libentity.example.commands.InvoiceActionCommand;
import com.libentity.example.model.InvoiceFilter;
import com.libentity.example.service.InvoiceService;
import com.libentity.example.service.InvoiceWithRateResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoice/action")
@RequiredArgsConstructor
public class InvoiceActionController {
    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceWithRateResponse> handleInvoiceAction(
            @RequestParam("invoiceId") String invoiceId, @RequestBody InvoiceActionCommand command) {
        InvoiceWithRateResponse response = invoiceService.handleAction(invoiceId, command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filter")
    public ResponseEntity<List<InvoiceWithRateResponse>> filterInvoices(@RequestBody InvoiceFilter filter) {
        List<InvoiceWithRateResponse> invoices = invoiceService.findByFilter(filter);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/create")
    public ResponseEntity<InvoiceWithRateResponse> createInvoice(@RequestBody CreateInvoiceCommand command) {
        InvoiceWithRateResponse response = invoiceService.createInvoice(command);
        return ResponseEntity.ok(response);
    }
}
