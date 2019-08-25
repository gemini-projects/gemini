package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;

import java.util.Optional;

public class EventContext {
    private final Transaction transaction;
    private EntityOperationContext entityOperationContext;
    private EntityRecord entityRecord;

    public EventContext(Transaction transaction,
                        EntityOperationContext entityOperationContext,
                        EntityRecord entityRecord) {
        this.transaction = transaction;
        this.entityOperationContext = entityOperationContext;
        this.entityRecord = entityRecord;
    }

    public Optional<Transaction> getTransaction() {
        return Optional.ofNullable(transaction);
    }

    public EntityRecord getEntityRecord() {
        return entityRecord;
    }

    public Optional<EntityOperationContext> getEntityOperationContext() {
        return Optional.ofNullable(entityOperationContext);
    }
}
