package it.at7.gemini.core.persistence;

import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.util.Collection;
import java.util.List;

public interface PersistenceSchemaManager {

    void beforeLoadSchema(Transaction transaction) throws GeminiException;

    default void handleSchemaStorage(Transaction transaction, Entity entity) throws GeminiException {
        handleSchemaStorage(transaction, List.of(entity));
    }

    /**
     * Create the storage containers for entities. For example if the persistence manager is a relational database
     * driver it creates tables and fields.
     *
     * @param transaction Gemini Transaction that atomically create the storage containers
     * @param entities    Target entities
     * @throws GeminiException
     */
    void handleSchemaStorage(Transaction transaction, Collection<Entity> entities) throws GeminiException;

    /**
     * Delete ENTITY records that are no longer required. Removing the entity schema cause the removal of its related
     * EntityRecords.
     *
     * @param entities             entities to remove
     * @param schemaFieldsToDelete foreign entity fields that can be checked (deleting its records pointing to the entity to remove)
     * @param transaction          transaction to use for the removal
     * @throws GeminiException
     */
    void deleteUnnecessaryEntites(Collection<Entity> entities, List<EntityField> schemaFieldsToDelete, Transaction transaction) throws GeminiException;

    /**
     * Delete FIELD records that are no longer required. Removing the field schema cause the removal of its related
     * FiELD EntityRecords.
     *
     * @param entity               target Entity
     * @param idFields             field ids no longer required
     * @param schemaFieldsToDelete foreign entity fields that can be checked (deleting its records pointing to the field to remove)
     * @param transaction          transaction to use for the removal
     * @throws GeminiException
     */
    void deleteUnnecessaryFields(Entity entity, List<Object> idFields, List<EntityField> schemaFieldsToDelete, Transaction transaction) throws GeminiException;

    void invokeCreateEntityStorageBefore(Entity entity, Transaction transaction) throws GeminiException;

    boolean entityStorageExists(Entity entity, Transaction transaction) throws GeminiException;

}
