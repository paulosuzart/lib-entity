package com.libentity.annotation.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.libentity.annotation.processor.EntityAnnotationProcessor;
import com.libentity.annotation.processor.EntityTypeRegistry;
import com.libentity.core.action.SyncActionExecutor;
import com.libentity.core.entity.EntityType;
import com.libentity.core.validation.ValidationContext;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class EntityAnnotationProcessorIntegrationTest {
    @Test
    void testReimbursementWorkflowExecution() {
        // 1. Build entity types from annotations
        EntityAnnotationProcessor processor = new EntityAnnotationProcessor();
        EntityTypeRegistry registry = processor.buildEntityTypes("com.libentity.annotation.integration");
        EntityType<ReimbursementState, Object> entityType =
                registry.entityTypes().get("Reimbursement");
        assertNotNull(entityType);

        // 2. Create executor (raw type)
        var executor = SyncActionExecutor.<ReimbursementState, Object>builder()
                .commandToActionResolver(registry.getCommandToActionNameResolver())
                .entityType(entityType)
                .build();

        // 3. Initial state
        var state = ReimbursementState.DRAFT;
        Object request = new Object();

        // 4. Submit reimbursement
        ValidationContext ctx1 = new ValidationContext();
        SubmitReimbursementCommand submitCmd = new SubmitReimbursementCommand();
        var submitResult = executor.execute(state, request, ctx1, submitCmd);
        assertEquals(ReimbursementState.SUBMITTED, submitResult.state());
        assertTrue(ctx1.getErrors().isEmpty());

        // 5. Approve reimbursement
        ValidationContext ctx2 = new ValidationContext();
        ApproveReimbursementCommand approveCmd = new ApproveReimbursementCommand();
        var approveResult = executor.execute(submitResult.state(), request, ctx2, approveCmd);
        assertEquals(ReimbursementState.APPROVED, approveResult.state());
        assertTrue(ctx2.getErrors().isEmpty());
    }
}
