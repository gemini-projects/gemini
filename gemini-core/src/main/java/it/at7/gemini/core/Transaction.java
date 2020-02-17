package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.time.LocalDateTime;
import java.util.*;

public abstract class Transaction implements AutoCloseable {

    private Set<Entity> updatedEntities = new HashSet<>();

    public abstract void open() throws GeminiException;

    public abstract void close() throws GeminiException;

    public abstract void commit() throws GeminiException;

    public abstract void rollback() throws GeminiException;

    public abstract Optional<TransactionCache> getTransactionCache();

    public abstract LocalDateTime getOpenTime();

    public void entityUpdate(Entity entity) {
        this.updatedEntities.add(entity);
    }

    public Collection<Entity> getUpdatedEntities() {
        return Collections.unmodifiableCollection(updatedEntities);
    }

}
