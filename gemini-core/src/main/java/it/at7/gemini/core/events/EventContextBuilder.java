package it.at7.gemini.core.events;

import it.at7.gemini.core.Transaction;

public class EventContextBuilder {

    private Transaction transaction;

    public EventContextBuilder(Transaction transaction) {
        this.transaction = transaction;
    }

    public EventContext build() {
        return new EventContext(transaction);
    }
}
