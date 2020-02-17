package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;

public interface TransactionManager {

    /**
     * Open Raw Transaction that you can use as the low level access to Datasource or DB
     *
     * @return the transaction
     * @throws GeminiException if something goes wrong
     */
    Transaction openRawTransaction() throws GeminiException;

    /**
     * Entity Managed Transaction perform Entity related transaction operations, such as update caches if some entity
     * was updated by the transaction (for example new record inserted or updated)
     *
     * @param callback code that use the new transaction
     * @param <T>      return type
     * @return a value from your callback
     * @throws GeminiException if something goes wrong
     */
    <T> T executeEntityManagedTransaction(TransactionCallback callback) throws GeminiException;

    /**
     * Entity Managed Transaction perform Entity related transaction operations, such as update caches if some entity
     * was updated by the transaction (for example new record inserted or updated)
     *
     * @param callback code that use the new transaction
     * @throws GeminiException
     */
    void executeEntityManagedTransaction(VoidTransactionCallback callback) throws GeminiException;
}
