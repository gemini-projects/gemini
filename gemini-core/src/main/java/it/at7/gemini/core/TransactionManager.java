package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;

import java.sql.SQLException;

public interface TransactionManager {

    Transaction openTransaction() throws GeminiException;

    <T> T executeInSingleTrasaction(TransactionCallback callback) throws GeminiException;

    void executeInSingleTrasaction(VoidTransactionCallback callback) throws GeminiException;

}
