package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;

public class EventContextBuilder {
    private Transaction transaction;
    private EntityOperationContext entityOperationContext;
    private EntityRecord entityRecord;
    private EntityRecord persistedEntityRecord;

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

    public EventContextBuilder withRecord(EntityRecord entityRecord) {
        this.entityRecord = entityRecord;
        return this;
    }

    public EventContextBuilder withPersistedRecord(EntityRecord entityRecord) {
        this.persistedEntityRecord = entityRecord;
        return this;
    }

    public EventContext build() {
        return new EventContext(transaction, entityOperationContext, entityRecord, persistedEntityRecord);
    }
}
