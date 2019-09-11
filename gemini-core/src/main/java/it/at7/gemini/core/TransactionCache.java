package it.at7.gemini.core;

import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.schema.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Transaction Cache can be used by the persistence manager to incrementally store data handled by the transaction.
 * It can be useful for example to avoid sub-sequent query to data already managed by the transaction, or
 * to avoid cycles while retrieving entity records.
 */
public class TransactionCache {

    private final Map<String, Map<Object, EntityRecord>> cache;

    public TransactionCache() {
        cache = new HashMap<>();
    }

    public void put(Entity entity, Object recordId, EntityRecord record) {
        Map<Object, EntityRecord> cacheByEntityID = cache.computeIfAbsent(entity.getName().toUpperCase(), k -> new HashMap<>());
        cacheByEntityID.put(recordId, record);
    }

    public void put(Entity entity, EntityRecord record) throws EntityRecordException {
        if (record.getID() == null)
            throw EntityRecordException.ID_RECORD_NOT_FOUND(record);
        put(entity, record.getID(), record);
    }

    public void put(EntityRecord record) throws EntityRecordException {
        put(record.getEntity(), record);
    }

    public Optional<EntityRecord> get(Entity entity, Object id) {
        Map<Object, EntityRecord> innerCache = cache.getOrDefault(entity.getName().toUpperCase(), Map.of());
        return Optional.ofNullable(innerCache.get(id));
    }
}
