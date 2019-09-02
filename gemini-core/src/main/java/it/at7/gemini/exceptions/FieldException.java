package it.at7.gemini.exceptions;

import it.at7.gemini.schema.FieldType;

import static it.at7.gemini.exceptions.FieldException.Code.FIELD_NOT_FOUND;
import static it.at7.gemini.exceptions.FieldException.Code.INVALID_LK_TYPE;

public class FieldException extends GeminiException {
    public enum Code {
        FIELD_NOT_FOUND,
        INVALID_LK_TYPE
    }

    public FieldException(Code errorCode, String message) {
        super(errorCode.name(), message);
    }

    public static FieldException FIELD_NOT_FOUND(String name) {
        return new FieldException(FIELD_NOT_FOUND, String.format("Field %s not found", name));
    }

    public static FieldException CANNOT_BE_LOGICAL_KEY(FieldType ft) {
        return new FieldException(INVALID_LK_TYPE, String.format("Field Type %s cannot be a logical key", ft.name()));
    }
}
