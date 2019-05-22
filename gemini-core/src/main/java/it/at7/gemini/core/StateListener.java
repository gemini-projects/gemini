package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import it.at7.gemini.exceptions.GeminiException;

import java.util.Optional;

public interface StateListener {
    default void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {}
}
