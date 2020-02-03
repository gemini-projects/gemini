package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;

@FunctionalInterface
public interface EntityRecordCallback {

    void exec(EntityRecord entityRecord) throws GeminiException;
}
