package it.at7.gemini.core;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.util.Collection;
import java.util.List;

public interface SchemaManager {
    
    Collection<Entity> getAllEntities();

    Entity getEntity(String entity);

    Module getModule(String module);

    /*
    void addNewRuntimeEntity(Entity newEntity, Transaction transaction) throws GeminiException;

    void addNewRuntimeEntityField(EntityField newEntityField, Transaction transaction) throws GeminiException;

    void deleteRuntimeEntity(Entity entityFromRecord, Transaction transaction) throws GeminiException;

    void deleteRuntimeEntityField(EntityField fieldFromRecord, Transaction transaction) throws GeminiException; */

    List<EntityField> getEntityReferenceFields(Entity targetEntity);
}
