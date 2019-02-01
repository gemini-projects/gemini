package it.at7.gemini.exceptions;

import it.at7.gemini.conf.State;

public class InvalidStateException extends GeminiException {
    public InvalidStateException(String message) {
        super(message);
    }

    public static  InvalidStateException STATE_LESS_THAN(State actual, State required){
        return new InvalidStateException(String.format("State %s isn't greather than  %s", actual, required));
    }
}
