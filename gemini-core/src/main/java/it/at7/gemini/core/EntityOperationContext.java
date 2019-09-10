package it.at7.gemini.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A class that is used to provide a contex to the EntityManager Operations. Contexts allows custom Gemini Modules
 * to add code/behaviours and information to the EntityManger lifecicle in order to easily extend the core features.
 * For example the authentication module add User information to the Context in order to add it to the modified or
 * created fields of entityRecord
 */
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
