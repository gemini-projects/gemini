package it.at7.gemini.exceptions;

import it.at7.gemini.schema.EntityField;

import static it.at7.gemini.exceptions.EntityFieldException.Code.ENTITYFIELD_ALREADY_FOUND;
import static it.at7.gemini.exceptions.EntityFieldException.Code.ENTITYFIELD_NOT_FOUND;

public class EntityFieldException extends GeminiException {
    public enum Code {
        ENTITYFIELD_ALREADY_FOUND,
        ENTITYFIELD_NOT_FOUND
    }
    private final EntityField entityField;

    public EntityFieldException(Code errorCode, String message, EntityField entityField) {
        super(errorCode.name(), message);
        this.entityField = entityField;
    }

    public EntityField getEntityField() {
        return entityField;
    }

    public static EntityFieldException ENTITYFIELD_ALREADY_FOUND(EntityField entityField) {
        return new EntityFieldException(ENTITYFIELD_ALREADY_FOUND, String.format("Field %s already found for entity %s", entityField.getName(), entityField.getEntity().getName()), entityField);
    }

    public static EntityFieldException ENTITYFIELD_NOT_FOUND(EntityField entityField) {
        return new EntityFieldException(ENTITYFIELD_NOT_FOUND, String.format("Field %s NOT FOUND for entity %s", entityField.getName(), entityField.getEntity().getName()), entityField);
    }
}