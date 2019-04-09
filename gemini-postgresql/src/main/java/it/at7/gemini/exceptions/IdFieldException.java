package it.at7.gemini.exceptions;

import it.at7.gemini.core.EntityRecord;

public class IdFieldException extends GeminiException {

    public IdFieldException(String message) {
        super(message);
    }

    public static IdFieldException ID_FIELD_REQUIRED(String action, EntityRecord entityRecord) {
        return new IdFieldException(String.format("Field ID required to action %s : %s", action, entityRecord.toString()));
    }
}
