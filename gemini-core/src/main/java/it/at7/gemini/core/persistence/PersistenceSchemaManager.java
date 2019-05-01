package it.at7.gemini.core.persistence;

import it.at7.gemini.core.Module;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PersistenceSchemaManager {

    void beforeLoadSchema(List<Module> modules, Transaction transaction) throws GeminiException;

    default void handleSchemaStorage(Transaction transaction, Entity entity) throws GeminiException {
        handleSchemaStorage(transaction, List.of(entity));
    }

    void handleSchemaStorage(Transaction transaction, Collection<Entity> entities) throws GeminiException;

    void deleteUnnecessaryEntites(Collection<Entity> entities, Transaction transaction) throws GeminiException;

    void deleteUnnecessaryFields(Entity entity, Set<EntityField> fields, Transaction transaction) throws GeminiException;

    void invokeCreateEntityStorageBefore(Entity entity, Transaction transaction) throws GeminiException;

    boolean entityStorageExists(Entity entity, Transaction transaction) throws GeminiException;

}
