package it.at7.gemini.core;

import it.at7.gemini.conf.State;

public interface StateListener {
    default void onChange(State previous, State actual){}
}
