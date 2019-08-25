package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;
import org.w3c.dom.Entity;

public class EventContextBuilder {
    private Transaction transaction;
    private EntityOperationContext entityOperationContext;
    private EntityRecord entityRecord;

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

    public EventContextBuilder with(EntityRecord entityRecord) {
        this.entityRecord = entityRecord;
        return this;
    }

    public EventContext build() {
        return new EventContext(transaction, entityOperationContext, entityRecord);
    }
}
