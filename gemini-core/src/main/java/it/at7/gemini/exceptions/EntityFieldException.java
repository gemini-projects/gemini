package it.at7.gemini.exceptions;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import static it.at7.gemini.exceptions.EntityFieldException.Code.ENTITYFIELD_ALREADY_FOUND;

public class EntityFieldException extends GeminiException {
    public enum Code {
        ENTITYFIELD_ALREADY_FOUND,
        ENTITYFIELD_NOT_FOUND,
        ENTITYMETAFIELD_NOT_FOUND
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
        return ENTITYFIELD_NOT_FOUND(entityField.getEntity(), entityField.getName());
    }

    public static EntityFieldNotFoundException ENTITYFIELD_NOT_FOUND(Entity entity, String fieldName) {
        return new EntityFieldNotFoundException(String.format("Field %s NOT FOUND for entity %s", fieldName, entity.getName()), null);
    }

    public static EntityMetaFieldNotFoundException ENTITYMETAFIELD_NOT_FOUND(EntityField entityField) {
        return ENTITYMETAFIELD_NOT_FOUND(entityField.getEntity(), entityField.getName());
    }

    public static EntityMetaFieldNotFoundException ENTITYMETAFIELD_NOT_FOUND(Entity entity, String fieldName) {
        return new EntityMetaFieldNotFoundException(String.format("Meta Field %s NOT FOUND for entity %s", fieldName, entity.getName()), null);
    }
}