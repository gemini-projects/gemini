package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.Transaction;

public class EventContextBuilder {
    private Transaction transaction;
    private EntityOperationContext entityOperationContext;

    public EventContextBuilder() {
    }

    public EventContextBuilder with(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public EventContextBuilder with(EntityOperationContext entityOperationContext) {
        this.entityOperationContext = entityOperationContext;
        return this;
    }

    public EventContext build() {
        return new EventContext(transaction, entityOperationContext);
    }
}
