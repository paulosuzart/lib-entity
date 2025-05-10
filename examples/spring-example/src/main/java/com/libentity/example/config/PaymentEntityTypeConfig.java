package com.libentity.example.config;

import com.libentity.annotation.processor.EntityAnnotationProcessor;
import com.libentity.annotation.processor.EntityTypeRegistry;
import com.libentity.core.action.ActionExecutor;
import com.libentity.core.action.SyncActionExecutor;
import com.libentity.core.entity.EntityType;
import com.libentity.example.payment.model.PaymentRequestContext;
import com.libentity.example.payment.model.PaymentState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("unchecked")
public class PaymentEntityTypeConfig {

    @Bean
    @Qualifier("entityTypeRegistryPayment")
    public EntityTypeRegistry entityTypeRegistry(ApplicationContext context) {
        var options = new EntityAnnotationProcessor.Options(context::getBean);

        EntityAnnotationProcessor processor = new EntityAnnotationProcessor(options);
        return processor.buildEntityTypes("com.libentity.example.payment.model");
    }

    @Bean
    @Qualifier("paymentEntityType")
    public EntityType<PaymentState, PaymentRequestContext> paymentEntityType(
            @Qualifier("entityTypeRegistryPayment") EntityTypeRegistry registry) {
        return (EntityType<PaymentState, PaymentRequestContext>)
                registry.entityTypes().get("Payment");
    }

    @Bean
    public ActionExecutor<PaymentState, PaymentRequestContext> paymentActionExecutor(
            @Qualifier("paymentEntityType") EntityType<PaymentState, PaymentRequestContext> paymentEntityType,
            @Qualifier("entityTypeRegistryPayment") EntityTypeRegistry registry) {
        return SyncActionExecutor.<PaymentState, PaymentRequestContext>builder()
                .entityType(paymentEntityType)
                .commandToActionResolver(registry.getCommandToActionNameResolver())
                .build();
    }
}
