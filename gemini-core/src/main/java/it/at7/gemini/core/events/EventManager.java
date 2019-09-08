package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;

/**
 * EventManager is the entry point to execute registered events related to Entity Records
 */
public interface EventManager {

    void beforeInsertFields(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    void onUpdateFields(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    void onInsertedRecord(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;
}
