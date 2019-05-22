package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import it.at7.gemini.exceptions.GeminiException;

import java.util.Optional;

public interface StateManager {
    void changeState(State state, Optional<Transaction> transaction) throws GeminiException;

    default void changeState(State state) throws GeminiException {
        changeState(state, Optional.empty());
    }

    void register(StateListener listener);

    State getActualState();
}
