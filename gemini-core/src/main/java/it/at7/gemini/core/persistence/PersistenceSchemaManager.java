package it.at7.gemini.core.persistence;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

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

    void deleteUnnecessaryEntites(Collection<Entity> entities, Transaction transaction) throws GeminiException;

    void deleteUnnecessaryFields(Entity entity, List<EntityRecord> fields, Transaction transaction) throws GeminiException;

    void invokeCreateEntityStorageBefore(Entity entity, Transaction transaction) throws GeminiException;

    boolean entityStorageExists(Entity entity, Transaction transaction) throws GeminiException;

}
