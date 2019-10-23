package it.at7.gemini.core;

import java.util.*;

/**
 * A class that is used to provide a contex to the EntityManager Operations. Contexts allows custom Gemini Modules
 * to add code/behaviours and information to the EntityManger lifecicle in order to easily extend the core features.
 * For example the authentication module add User information to the Context in order to add it to the modified or
 * created fields of entityRecord
 */
public class EntityOperationContext {
    public static final EntityOperationContext EMPTY = new EntityOperationContext();
    private final Map<String, Object> moduleContexts;
    private final Set<String> contextFlag;


    public EntityOperationContext() {
        this.moduleContexts = new HashMap<>();
        this.contextFlag = new HashSet<>();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getModuleEntityOpContext(ModuleBase module) {
        return getModuleEntityOpContext(module.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getModuleEntityOpContext(String moduleName) {
        return (Optional<T>) Optional.ofNullable(moduleContexts.get(moduleName));
    }

    public void putModuleEntityOpContext(ModuleBase module, Object context) {
        moduleContexts.put(module.getName(), context);
    }

    public void putFlag(String flagName) {
        contextFlag.add(flagName.toUpperCase());
    }

    public boolean hasFlag(String flagName) {
        return contextFlag.contains(flagName.toUpperCase());
    }
}
