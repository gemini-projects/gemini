package it.at7.gemini.exceptions;

import it.at7.gemini.schema.Entity;

import static it.at7.gemini.exceptions.EntityException.Code.*;

public class EntityException extends GeminiException {
    private final Entity entity;
    private final String entityStr;

    public enum Code {
        ENTITY_FOUND,
        ENTITY_NOT_FOUND,
        ENTITY_FIELD_FOUND,
        API_NOT_ALLOWED_ON_EMBEDABLE,
        API_ALLOWED_ONLY_ON_ONEREC
    }

    public EntityException(EntityException.Code errorCode, String message, Entity entity) {
        super(errorCode.name(), message);
        this.entity = entity;
        this.entityStr = entity.getName();
    }

    public EntityException(EntityException.Code errorCode, String message, String entity) {
        super(errorCode.name(), message);
        this.entityStr = entity;
        this.entity = null;
    }

    public static EntityException ENTITY_FOUND(Entity entity) {
        return new EntityException(ENTITY_FOUND, String.format("Already found Entity %s ", entity.getName()), entity);
    }

    public static EntityException ENTITY_NOT_FOUND(String entity) {
        return new EntityException(ENTITY_NOT_FOUND, String.format("Entity %s not found", entity), entity);
    }

    public static EntityException API_NOT_ALLOWED_ON_EMBEDABLE(String entity) {
        return new EntityException(API_NOT_ALLOWED_ON_EMBEDABLE, String.format("Entity %s is embedable - API not allowed", entity), entity);
    }

    public static EntityException API_ALLOWED_ONLY_ON_ONEREC(String entity) {
        return new EntityException(API_ALLOWED_ONLY_ON_ONEREC, String.format("Entity %s is not one record - API not allowed", entity), entity);
    }
}
