package it.at7.gemini.core.persistence;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static it.at7.gemini.core.EntityResolutionContext.DEFAULT;

public interface PersistenceEntityManager {

    List<EntityRecord> getEntityRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFieldValueType, EntityResolutionContext resolutionContext, Transaction transaction) throws GeminiException;

    default List<EntityRecord> getEntityRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFieldValueType, Transaction transaction) throws GeminiException {
        return getEntityRecordsMatching(entity, filterFieldValueType, DEFAULT, transaction);
    }

    List<EntityRecord> getEntityRecordsMatching(Entity entity, FilterContext filterContext, EntityResolutionContext entityResolutionContext, Transaction transaction) throws GeminiException;

    default List<EntityRecord> getEntityRecordsMatching(Entity entity, FilterContext filterContext, Transaction transaction) throws GeminiException {
        return getEntityRecordsMatching(entity, filterContext, DEFAULT, transaction);
    }

    default Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, EntityRecord logicalKeyRecord, Transaction transaction) throws GeminiException {
        return getEntityRecordByLogicalKey(entity, logicalKeyRecord.getLogicalKeyValue(), transaction);
    }

    Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, Collection<? extends DynamicRecord.FieldValue> logicalKey, Transaction transaction) throws GeminiException;

    Optional<EntityRecord> getEntityRecordByLogicalKey(EntityRecord record, Transaction transaction) throws GeminiException;

    Optional<EntityRecord> getEntityRecordById(Entity entity, long recordId, Transaction transaction) throws GeminiException;

    EntityRecord createNewEntityRecord(EntityRecord record, Transaction transaction) throws GeminiException;

    EntityRecord updateEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException;

    void deleteEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException;

    EntityRecord createOrUpdateEntityRecord(EntityRecord entityRecord, Transaction transaction) throws GeminiException;

}
