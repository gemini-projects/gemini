package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;

import java.time.LocalDateTime;

public interface Transaction extends AutoCloseable {

    void open() throws GeminiException;

    void close() throws GeminiException;

    void commit() throws GeminiException;

    void rollback() throws GeminiException;

    LocalDateTime getOpenTime();

}
