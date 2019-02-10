package it.at7.gemini.exceptions;

import it.at7.gemini.core.DynamicRecord;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.EntityReferenceRecord;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.Set;

import static it.at7.gemini.exceptions.EntityRecordException.Code.*;

public class EntityRecordException extends GeminiException {
    public enum Code {
        MULTIPLE_LK_FOUND,
        LK_NOTFOUND,
        INSERTED_RECORD_NOT_FOUND
    }

    private final Entity entity;
    private final Collection<? extends DynamicRecord.FieldValue> lk;
    private final Code errorCode;

    public EntityRecordException(Code errorCode, Entity entity, Collection<? extends DynamicRecord.FieldValue> lk, String message) {
        super(errorCode.name(), message);
        this.errorCode = errorCode;
        this.entity = entity;
        this.lk = lk;
    }

    public Code getErrorCode() {
        return errorCode;
    }

    public Entity getEntity() {
        return entity;
    }

    public Collection<? extends DynamicRecord.FieldValue> getLk() {
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

    public static EntityRecordException MULTIPLE_LK_FOUND(Entity entity, Collection<? extends DynamicRecord.FieldValue> lk) {
        return new EntityRecordException(MULTIPLE_LK_FOUND, entity, lk, String.format("Found multiple DynamicRecord for Logical Key of %s : %s", entity.getName(), lk.toString()));
    }

    public static EntityRecordException LK_NOTFOUND(Entity entity, Collection<? extends DynamicRecord.FieldValue> lk) {
        return new EntityRecordException(LK_NOTFOUND, entity, lk, String.format("Logical Key for entity %s not found: %s ", entity.getName(), lk.toString()));
    }

    public static EntityRecordException INSERTED_RECORD_NOT_FOUND(Entity entity, Set<EntityRecord.EntityFieldValue> lk) {
        return new EntityRecordException(INSERTED_RECORD_NOT_FOUND, entity, lk, String.format("Inserted record for entity %s not found: %s ", entity.getName(), lk.toString()));
    }

}
