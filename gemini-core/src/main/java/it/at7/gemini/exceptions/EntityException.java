package it.at7.gemini.exceptions;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

public class EntityException extends GeminiException {
    public EntityException(String message) {
        super(message);
    }

    public static EntityException ENTITY_FOUND(Entity entity) {
        return new EntityException(String.format("Already found Entity %s ", entity.getName()));
    }

    public static EntityException ENTITY_NOT_FOUND(String entity) {
        return new EntityException(String.format("Entity %s not found", entity));
    }

    public static EntityException ENTITY_FIELD_FOUND(EntityField entityField) {
        return new EntityException(String.format("Already found Field %s for Entity %s ", entityField.getName(), entityField.getEntity().getName()));
    }
}
