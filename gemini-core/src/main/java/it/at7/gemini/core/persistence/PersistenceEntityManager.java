package it.at7.gemini.core.persistence;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersistenceEntityManager {

    void getALLEntityRecords(Entity entity, Transaction transaction, EntityRecordCallback callback) throws GeminiException;

    List<EntityRecord> getEntityRecordsMatching(Entity entity, Collection<? extends FieldValue> filterFieldValueType, Transaction transaction) throws GeminiException;

    List<EntityRecord> getEntityRecordsMatching(Entity entity, FilterContext filterContex, Transaction transaction) throws GeminiException;

    default Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, EntityRecord logicalKeyRecord, Transaction transaction) throws GeminiException {
        return getEntityRecordByLogicalKey(entity, logicalKeyRecord.getLogicalKeyValue(), transaction);
    }

    default Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, EntityReferenceRecord entityReferenceRecord, Transaction transaction) throws GeminiException {
        return getEntityRecordByLogicalKey(entity, entityReferenceRecord.getLogicalKeyRecord().getFieldValues(), transaction);
    }

    Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, Collection<? extends FieldValue> logicalKey, Transaction transaction) throws GeminiException;

    Optional<EntityRecord> getEntityRecordByLogicalKey(EntityRecord record, Transaction transaction) throws GeminiException;

    Optional<EntityRecord> getEntityRecordById(Entity entity, long recordId, Transaction transaction) throws GeminiException;

    Optional<EntityRecord> getEntityRecordByUUID(Entity entity, UUID uuid, Transaction transaction) throws GeminiException;

    EntityRecord createNewEntityRecord(EntityRecord record, Transaction transaction) throws GeminiException;

    void createNewEntityRecordNoResults(EntityRecord record, Transaction transaction) throws GeminiException;

    void createEntityRecordBatch(Collection<EntityRecord> records, Transaction transaction) throws GeminiException;

    EntityRecord updateEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException;

    void deleteEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException;

    EntityRecord createOrUpdateEntityRecord(EntityRecord entityRecord, Transaction transaction) throws
            GeminiException;

    UUID getUUIDforEntityRecord(EntityRecord record) throws GeminiException;

    EntityRecord getEntityRecordSingleton(Entity entity, Transaction transaction) throws GeminiException;

    long countEntityRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;
}
