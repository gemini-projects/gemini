package it.at7.gemini.exceptions;

import static it.at7.gemini.exceptions.FieldException.Code.FIELD_NOT_FOUND;

public class FieldException extends GeminiException {
    public enum Code {
        FIELD_NOT_FOUND,
    }

    public FieldException(Code errorCode, String message) {
        super(errorCode.name(), message);
    }

    public static FieldException FIELD_NOT_FOUND(String name) {
        return new FieldException(FIELD_NOT_FOUND, String.format("Field %s not found", name));
    }
}
