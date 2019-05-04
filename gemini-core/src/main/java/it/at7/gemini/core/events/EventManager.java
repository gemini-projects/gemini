package it.at7.gemini.core.events;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;

public interface EventManager {
    void beforeInsertFields(EntityRecord record, Transaction transaction) throws GeminiException;

    void onUpdateFields(EntityRecord record, Transaction transaction) throws GeminiException;
}
