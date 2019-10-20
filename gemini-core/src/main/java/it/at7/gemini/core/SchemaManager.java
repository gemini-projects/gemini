package it.at7.gemini.core;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface SchemaManager {

    Collection<Entity> getAllEntities();

    Entity getEntity(String entity);

    Collection<ModuleBase> getAllModules();

    default Map<String, ModuleBase> getModulesMap() {
        Collection<ModuleBase> modules = getAllModules();
        return modules.stream().collect(Collectors.toMap(ModuleBase::getName, Function.identity()));
    }


    /*
    void addNewRuntimeEntity(Entity newEntity, Transaction transaction) throws GeminiException;

    void addNewRuntimeEntityField(EntityField newEntityField, Transaction transaction) throws GeminiException;

    void deleteRuntimeEntity(Entity entityFromRecord, Transaction transaction) throws GeminiException;

    void deleteRuntimeEntityField(EntityField fieldFromRecord, Transaction transaction) throws GeminiException; */

    List<EntityField> getEntityReferenceFields(Entity targetEntity);
}
