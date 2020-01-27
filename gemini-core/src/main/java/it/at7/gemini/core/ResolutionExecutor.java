package it.at7.gemini.core;

import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldRef;
import it.at7.gemini.schema.FieldResolutionDef;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ResolutionExecutor {
    private final EntityRecord entityRecord;
    private final PersistenceEntityManager persistenceEntityManager;
    private final SchemaManager schemaManager;
    private final Transaction transaction;
    private final EventType resolutionEventType;

    private ResolutionExecutor(EntityRecord entityRecord, PersistenceEntityManager persistenceEntityManager, SchemaManager schemaManager, Transaction transaction, EventType resolutionEventType) {
        this.entityRecord = entityRecord;
        this.persistenceEntityManager = persistenceEntityManager;
        this.schemaManager = schemaManager;
        this.transaction = transaction;
        this.resolutionEventType = resolutionEventType;
    }

    public void run() throws GeminiException {
        Entity targetEntity = entityRecord.getEntity();
        List<EntityField> targetEntityFields = schemaManager.getEntityReferenceFields(targetEntity);
        switch (resolutionEventType) {
            case DELETE:
                handleDelete(targetEntityFields);
                break;
        }
    }

    private void handleDelete(List<EntityField> targetEntityFields) throws GeminiException {
        Object id = entityRecord.getID();
        Set<EntityFieldValue> logicalKeyValue = entityRecord.getLogicalKeyValue();
        for (EntityField field : targetEntityFields) {
            FieldResolutionDef.VALUE resolutionType = getResolutionType(field);
            EntityReferenceRecord entityReferenceRecord = EntityReferenceRecord.fromPKValue(entityRecord.getEntity(), id);
            entityReferenceRecord.addLogicalKeyValues(logicalKeyValue);
            EntityFieldValue targetFieldValue = new EntityFieldValue(field, entityReferenceRecord);
            List<EntityRecord> recordsToResolve = persistenceEntityManager.getEntityRecordsMatching(field.getEntity(), Set.of(targetFieldValue), transaction);
            switch (resolutionType) {
                case EMPTY:
                    for (EntityRecord record : recordsToResolve) {
                        record.put(field, null);
                        persistenceEntityManager.updateEntityRecordByID(record, transaction);
                    }
                    break;

            }
            // EntityRecord.RecordConverters.logicalKeyFromObject(fieldResolutionEntity, )
            // entityManager.getEntityRecordsMatching(fieldResolutionEntity, Set.of(),)

        }
    }

    private FieldResolutionDef.VALUE getResolutionType(EntityField field) throws GeminiException {
        Entity fieldResolutionEntity = schemaManager.getEntity(Entity.FIELD_RESOLUTION);
        EntityRecord fieldResolutionRec = new EntityRecord(fieldResolutionEntity);
        DynamicRecord fieldRecord = new DynamicRecord();
        fieldRecord.put(FieldRef.FIELDS.NAME, field.getName().toLowerCase());
        fieldRecord.put(FieldRef.FIELDS.ENTITY, field.getEntity().getName());
        fieldResolutionRec.put(FieldResolutionDef.FIELDS.FIELD, fieldRecord);
        fieldResolutionRec.put(FieldResolutionDef.FIELDS.CODE, "DELETE");
        try {
            Optional<EntityRecord> resRecordOpt = persistenceEntityManager.getEntityRecordByLogicalKey(fieldResolutionEntity, fieldResolutionRec.getLogicalKeyValue(), transaction);
            if (!resRecordOpt.isPresent()) {
                return FieldResolutionDef.VALUE.EMPTY;
            }
            EntityRecord resRecord = resRecordOpt.get();
            String resolutionType = resRecord.get(FieldResolutionDef.FIELDS.VALUE);
            return FieldResolutionDef.VALUE.valueOf(resolutionType.toUpperCase());
        } catch (NullPointerException e) {
            return FieldResolutionDef.VALUE.EMPTY;
        }
    }

    public static ResolutionExecutor forDelete(EntityRecord entityRecord, PersistenceEntityManager persistenceEntityManager, SchemaManager schemaManager, Transaction transaction) {
        return new ResolutionExecutor(entityRecord, persistenceEntityManager, schemaManager, transaction, EventType.DELETE);
    }

    enum EventType {
        DELETE
    }
}
