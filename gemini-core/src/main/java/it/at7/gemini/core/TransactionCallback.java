package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import org.springframework.lang.Nullable;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {

    @Nullable
    T doInTransaction(Transaction transaction) throws GeminiException;
}
