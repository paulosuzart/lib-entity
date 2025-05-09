package com.libentity.annotation.processor;

import com.libentity.core.entity.EntityType;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry of entity types and command-to-action name mappings.
 */
@SuppressWarnings("rawtypes")
public record EntityTypeRegistry(Map<String, EntityType> entityTypes, Map<Class<?>, String> commandToActionName) {

    /**
     * Returns a function that maps a command class to its corresponding action name.
     *
     * Can be used in conjuction with SyncActionExecutor.
     * @see SyncActionExecutor
     * @return
     */
    public Function<Object, String> getCommandToActionNameResolver() {
        return (o) -> commandToActionName.get(o.getClass());
    }
}
