package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;

import java.sql.SQLException;

@FunctionalInterface
public interface VoidTransactionCallback {

    void doInTransaction(Transaction transaction) throws  GeminiException;

}
