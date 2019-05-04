package it.at7.gemini.core.events;

import it.at7.gemini.core.Transaction;

public class EventContext {
    private final Transaction transaction;

    public EventContext(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
