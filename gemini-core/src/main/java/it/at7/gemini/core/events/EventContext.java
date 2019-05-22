package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.Transaction;

import java.util.Optional;

public class EventContext {
    private final Transaction transaction;
    private EntityOperationContext entityOperationContext;

    public EventContext(Transaction transaction, EntityOperationContext entityOperationContext) {
        this.transaction = transaction;
        this.entityOperationContext = entityOperationContext;
    }

    public Optional<Transaction> getTransaction() {
        return Optional.ofNullable(transaction);
    }


    public Optional<EntityOperationContext> getEntityOperationContext() {
        return Optional.ofNullable(entityOperationContext);
    }
}
