package it.at7.gemini.core;

import it.at7.gemini.conf.State;

public interface StateManager {
    void changeState(State state);

    void register(StateListener listener);

    State getActualState();
}
