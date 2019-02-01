package it.at7.gemini.core.persistence;

import it.at7.gemini.core.Module;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PersistenceSchemaManager {

    void beforeLoadSchema(Map<String, Module> modules, Transaction transaction) throws GeminiException, SQLException, IOException;

    default void handleSchemaStorage(Transaction transaction, Entity entity) throws GeminiException {
        handleSchemaStorage(transaction, List.of(entity));
    }

    void handleSchemaStorage(Transaction transaction, Collection<Entity> entities) throws GeminiException;

    void deleteUnnecessaryEntites(Collection<Entity> entities, Transaction transaction) throws SQLException;

    void deleteUnnecessaryFields(Entity entity, Set<EntityField> fields, Transaction transaction) throws SQLException;

    void invokeCreateEntityStorageBefore(Entity entity, Transaction transaction) throws SQLException, GeminiException;

    boolean entityStorageExists(Entity entity, Transaction transaction) throws SQLException, GeminiException;

}
