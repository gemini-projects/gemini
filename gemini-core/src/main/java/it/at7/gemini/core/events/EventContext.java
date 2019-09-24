package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class EventContext {
    private final Transaction transaction;
    private EntityOperationContext entityOperationContext;
    private EntityRecord entityRecord;
    private final EntityRecord persistedEntityRecord;

    public EventContext(Transaction transaction,
                        EntityOperationContext entityOperationContext,
                        EntityRecord entityRecord,
                        @Nullable EntityRecord persistedEntityRecord) {
        this.transaction = transaction;
        this.entityOperationContext = entityOperationContext;
        this.entityRecord = entityRecord;
        this.persistedEntityRecord = persistedEntityRecord;
    }

    public Optional<Transaction> getTransaction() {
        return Optional.ofNullable(transaction);
    }

    /**
     * @ return the entity record that is involved in the operation
     */
    public EntityRecord getEntityRecord() {
        return entityRecord;
    }

    public Optional<EntityOperationContext> getEntityOperationContext() {
        return Optional.ofNullable(entityOperationContext);
    }

    /**
     * @return If present (for example an update operation) return the already existent EntityRecord before the actual
     * operation change
     */
    public Optional<EntityRecord> getPersistedEntityRecord() {
        return Optional.ofNullable(persistedEntityRecord);
    }
}
