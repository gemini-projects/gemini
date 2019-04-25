package it.at7.gemini.exceptions.gui;

import it.at7.gemini.exceptions.GeminiException;

import static it.at7.gemini.exceptions.gui.ComponentException.Code.*;

public class ComponentException extends GeminiException {

    public enum Code {
        EVENT_NOT_FOUND,
        COMPONENT_NOT_FOUND
    }

    private final String component;
    private final String event;


    public ComponentException(Code errorCodeName, String message, String component, String event) {
        super(errorCodeName.name(), message);
        this.component = component;
        this.event = event;
    }

    public static ComponentException EVENT_NOT_FOUND(String component, String event) {
        return new ComponentException(EVENT_NOT_FOUND, String.format("Events %s not found for component %s", event, component), component, event);
    }

    public static ComponentException COMPONENT_NOT_FOUND(String component, String event) {
        return new ComponentException(COMPONENT_NOT_FOUND, String.format("Component %s not found - trying to handle events", component, event), component, event);
    }
}
