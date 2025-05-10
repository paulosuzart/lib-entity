package com.libentity.example.payment.service;

import com.libentity.core.action.ActionExecutor;
import com.libentity.core.validation.ValidationContext;
import com.libentity.example.payment.command.ApprovePaymentCommand;
import com.libentity.example.payment.model.PaymentAggregate;
import com.libentity.example.payment.model.PaymentRequestContext;
import com.libentity.example.payment.model.PaymentState;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final ActionExecutor<PaymentState, PaymentRequestContext> actionExecutor;
    private final ModelMapper modelMapper;

    public PaymentAggregate approvePayment(ApprovePaymentCommand command) {
        // complete fake payment to approve
        PaymentAggregate paymentAgg = new PaymentAggregate();
        paymentAgg.setId(UUID.randomUUID());
        paymentAgg.setAmount(BigDecimal.valueOf(1200.97));
        paymentAgg.setState(PaymentState.DRAFT);

        PaymentAggregate newPaymentAgg = modelMapper.map(paymentAgg, PaymentAggregate.class);
        PaymentRequestContext requestContext = new PaymentRequestContext(paymentAgg, newPaymentAgg);
        var validationContext = new ValidationContext();
        var result = actionExecutor.execute(PaymentState.DRAFT, requestContext, validationContext, command);
        if (validationContext.hasErrors()) {
            throw new IllegalArgumentException(validationContext.getErrors().toString());
        }
        requestContext.newPayment().setState(result.state());
        log.info("Payment approved {}, id {}", result.state(), newPaymentAgg.getId());

        return requestContext.newPayment();
    }
}
