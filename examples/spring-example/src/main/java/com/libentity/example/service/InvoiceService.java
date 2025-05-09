package com.libentity.example.service;

import com.libentity.core.action.ActionCommand;
import com.libentity.core.action.ActionExecutor;
import com.libentity.core.action.ActionResult;
import com.libentity.core.validation.ValidationContext;
import com.libentity.example.commands.CreateInvoiceCommand;
import com.libentity.example.model.Invoice;
import com.libentity.example.model.InvoiceFilter;
import com.libentity.example.model.InvoiceRequestContext;
import com.libentity.example.model.InvoiceState;
import com.libentity.example.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final ActionExecutor<InvoiceState, InvoiceRequestContext> actionExecutor;
    private final InvoiceRepository invoiceRepository;
    private final ModelMapper modelMapper;

    public InvoiceWithRateResponse handleAction(String invoiceId, ActionCommand command) {
        Invoice invoice = invoiceRepository.loadById(Long.valueOf(invoiceId));
        Invoice newInvoice = modelMapper.map(invoice, Invoice.class);
        ValidationContext ctx = new ValidationContext();

        ActionResult<InvoiceState, InvoiceRequestContext, ActionCommand> result = actionExecutor.execute(
                invoice.getState(), new InvoiceRequestContext(invoice, newInvoice), ctx, command);

        InvoiceWithRateResponse response = new InvoiceWithRateResponse();
        response.setInvoice(result.request().newInvoice());
        response.setExchangeRate(BigDecimal.valueOf(1.23)); // Example rate
        // Set allowed actions after state change
        response.setAllowedActions(actionExecutor.getAllowedActions(result.state(), result.request()));
        return response;
    }

    public List<InvoiceWithRateResponse> findByFilter(InvoiceFilter filter) {
        return invoiceRepository.findByFilter(filter).stream()
                .map(invoice -> {
                    InvoiceWithRateResponse response = new InvoiceWithRateResponse();
                    response.setInvoice(invoice);
                    response.setExchangeRate(BigDecimal.valueOf(1.23)); // Example rate
                    // Set allowed actions for each invoice
                    response.setAllowedActions(actionExecutor.getAllowedActions(
                            invoice.getState(), new InvoiceRequestContext(invoice, invoice)));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public InvoiceWithRateResponse createInvoice(CreateInvoiceCommand command) {
        // Use action executor to handle validation and mutation
        Invoice invoice = new Invoice();
        ValidationContext ctx = new ValidationContext();
        ActionResult<InvoiceState, InvoiceRequestContext, CreateInvoiceCommand> ignored =
                (ActionResult<InvoiceState, InvoiceRequestContext, CreateInvoiceCommand>)
                        actionExecutor.execute(null, new InvoiceRequestContext(invoice, invoice), ctx, command);
        if (!ctx.getErrors().isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + ctx.getErrors());
        }
        invoiceRepository.save(invoice);
        InvoiceWithRateResponse response = new InvoiceWithRateResponse();
        response.setInvoice(invoice);
        response.setExchangeRate(java.math.BigDecimal.valueOf(1.23)); // Example rate
        // Set allowed actions after creation
        response.setAllowedActions(
                actionExecutor.getAllowedActions(invoice.getState(), new InvoiceRequestContext(invoice, invoice)));
        return response;
    }
}
