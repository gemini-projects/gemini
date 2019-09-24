package it.at7.gemini.exceptions;

import it.at7.gemini.core.*;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static it.at7.gemini.exceptions.EntityRecordException.Code.*;

public class EntityRecordException extends GeminiException {
    public enum Code {
        MULTIPLE_LK_FOUND,
        LK_NOTFOUND,
        INSERTED_RECORD_NOT_FOUND,
        UUID_NOTFOUND,
        ONERECORD_ENTITY_MUSTEXIST,
        ID_RECORD_NOT_FOUND,
        EMPTY_LK_IN_RECORD
    }

    private final Entity entity;
    private final Collection<? extends FieldValue> lk;
    private final Code errorCode;
    private final UUID uuid;

    public EntityRecordException(Code errorCode, Entity entity, Collection<? extends FieldValue> lk, String message) {
        super(errorCode.name(), message);
        this.errorCode = errorCode;
        this.entity = entity;
        this.lk = lk;
        this.uuid = null;
    }

    public EntityRecordException(Code errorCode, Entity entity, UUID uuid, String message) {
        super(errorCode.name(), message);
        this.errorCode = errorCode;
        this.entity = entity;
        this.lk = null;
        this.uuid = uuid;
    }

    public EntityRecordException(Code errorCode, Entity entity, String message) {
        super(errorCode.name(), message);
        this.errorCode = errorCode;
        this.entity = entity;
        this.lk = null;
        this.uuid = null;
    }

    public Code getErrorCode() {
        return errorCode;
    }

    public Entity getEntity() {
        return entity;
    }

    public Collection<? extends FieldValue> getLk() {
        return lk;
    }

    public static EntityRecordException MULTIPLE_LK_FOUND(EntityReferenceRecord referenceRecord) {
        Entity entity = referenceRecord.getEntity();
        DynamicRecord logicalKeyRecord = referenceRecord.getLogicalKeyRecord();
        return MULTIPLE_LK_FOUND(entity, logicalKeyRecord.getFieldValues());
    }

    public static EntityRecordException MULTIPLE_LK_FOUND(EntityRecord entityRecord) {
        return MULTIPLE_LK_FOUND(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    public static EntityRecordException MULTIPLE_LK_FOUND(Entity entity, Collection<? extends FieldValue> lk) {
        return new EntityRecordException(MULTIPLE_LK_FOUND, entity, lk, String.format("Found multiple Records for Logical Key of %s : %s", entity.getName(), lk.toString()));
    }

    public static EntityRecordException LK_NOTFOUND(Entity entity, Collection<? extends FieldValue> lk) {
        return new EntityRecordException(LK_NOTFOUND, entity, lk, String.format("Logical Key for entity %s not found: %s ", entity.getName(), lk.toString()));
    }

    public static EntityRecordException INSERTED_RECORD_NOT_FOUND(Entity entity, Set<EntityFieldValue> lk) {
        return new EntityRecordException(INSERTED_RECORD_NOT_FOUND, entity, lk, String.format("Inserted record for entity %s not found: %s ", entity.getName(), lk.toString()));
    }

    public static EntityRecordException UUID_NOTFOUND(Entity entity, UUID uuid) {
        return new EntityRecordException(UUID_NOTFOUND, entity, uuid, String.format("UUID for entity %s not found: %s ", entity.getName(), uuid.toString()));
    }

    public static EntityRecordException ONERECORD_ENTITY_MUSTEXIST(Entity entity) {
        return new EntityRecordException(ONERECORD_ENTITY_MUSTEXIST, entity, String.format("Entity %s is ONEREC - must contain one record", entity.getName()));
    }

    public static EntityRecordException ID_RECORD_NOT_FOUND(EntityRecord entityRecord) {
        return new EntityRecordException(ID_RECORD_NOT_FOUND, entityRecord.getEntity(), entityRecord.getLogicalKeyValue(), "EntityRecord ID not found");
    }

    public static EntityRecordException EMPTY_LK_IN_RECORD(EntityRecord entityRecord) {
        return new EntityRecordException(EMPTY_LK_IN_RECORD, entityRecord.getEntity(), String.format("Provided a record for entity %s withRecord empty logical key -  %s", entityRecord.getEntity().getName(), entityRecord.toString()));
    }
}
