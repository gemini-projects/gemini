package it.at7.gemini.exceptions;

import it.at7.gemini.schema.EntityField;

import static it.at7.gemini.exceptions.EntityFieldException.Code.ENTITYFIELD_NOT_FOUND;

public class EntityFieldNotFoundException extends EntityFieldException {

    public EntityFieldNotFoundException(String message, EntityField entityField) {
        super(ENTITYFIELD_NOT_FOUND, message, entityField);
    }
}
