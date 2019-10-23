package it.at7.gemini.core;

import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface SchemaManager {

    Collection<Entity> getAllEntities();

    Entity getEntity(String entity);

    Collection<ModuleBase> getAllModules();

    Optional<ModuleBase> getModule(String moduleName);

    default Map<String, ModuleBase> getModulesMap() {
        Collection<ModuleBase> modules = getAllModules();
        return modules.stream().collect(Collectors.toMap(ModuleBase::getName, Function.identity()));
    }

    List<EntityField> getEntityReferenceFields(Entity targetEntity);

    void addOrUpdateDynamicSchema(ModuleBase module, RawSchema rawSchema, EntityOperationContext operationContext, Transaction transaction) throws GeminiException;

    void updateDynamicSchema(ModuleBase module, RawSchema rawSchema, Transaction transaction) throws GeminiException;
}
