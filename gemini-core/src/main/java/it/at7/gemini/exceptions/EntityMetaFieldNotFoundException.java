package it.at7.gemini.exceptions;

import it.at7.gemini.schema.EntityField;

public class EntityMetaFieldNotFoundException extends EntityFieldException {
    public EntityMetaFieldNotFoundException(String message, EntityField entityField) {
        super(Code.ENTITYMETAFIELD_NOT_FOUND, message, entityField);
    }
}
