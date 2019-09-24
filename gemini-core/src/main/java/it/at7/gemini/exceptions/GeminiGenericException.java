package it.at7.gemini.exceptions;

import static it.at7.gemini.exceptions.GeminiGenericException.Code.GENERIC_EXCEPTION;

public class GeminiGenericException extends GeminiException {
    enum Code {
        GENERIC_EXCEPTION
    }

    public GeminiGenericException(Throwable t) {
        super(GENERIC_EXCEPTION.name(), t);
    }


    public GeminiGenericException(Exception e) {
        super(GENERIC_EXCEPTION.name(), e);
    }

    public static GeminiGenericException wrap(Exception e) {
        return new GeminiGenericException(e);
    }

    public static GeminiGenericException wrap(Throwable t) {
        return new GeminiGenericException(t);
    }
}
