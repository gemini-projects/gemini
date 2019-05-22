package it.at7.gemini.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityOperationContext {
    public static final EntityOperationContext EMPTY = new EntityOperationContext();
    private final Map<String, Object> moduleContexts;

    public EntityOperationContext() {
        this.moduleContexts = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getModuleEntityOpContext(Module module) {
        return getModuleEntityOpContext(module.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getModuleEntityOpContext(String moduleName) {
        return (Optional<T>) Optional.ofNullable(moduleContexts.get(moduleName));
    }


    public void putModuleEntityOpContext(Module module, Object context) {
        moduleContexts.put(module.getName(), context);
    }
}
