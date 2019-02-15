package it.at7.gemini.core.persistence;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static it.at7.gemini.core.EntityResolutionContext.DEFAULT;

public interface PersistenceEntityManager {

    List<EntityRecord> getRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFieldValueType, EntityResolutionContext resolutionContext, Transaction transaction) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFieldValueType, Transaction transaction) throws GeminiException {
        return getRecordsMatching(entity, filterFieldValueType, DEFAULT, transaction);
    }

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, EntityResolutionContext entityResolutionContext, Transaction transaction) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, Transaction transaction) throws GeminiException {
        return getRecordsMatching(entity, filterContext, DEFAULT, transaction);
    }

    default Optional<EntityRecord> getRecordByLogicalKey(Entity entity, EntityRecord logicalKeyRecord, Transaction transaction) throws GeminiException {
        return getRecordByLogicalKey(entity, logicalKeyRecord.getLogicalKeyValue(), transaction);
    }

    Optional<EntityRecord> getRecordByLogicalKey(Entity entity, Collection<? extends DynamicRecord.FieldValue> logicalKey, Transaction transaction) throws GeminiException;

    Optional<EntityRecord> getRecordByLogicalKey(EntityRecord record, Transaction transaction) throws GeminiException;

    EntityRecord createNewEntityRecord(EntityRecord record, Transaction transaction) throws GeminiException;

    EntityRecord updateEntityRecord(EntityRecord record, Transaction transaction) throws GeminiException;

    void deleteEntity(EntityRecord record, Transaction transaction) throws GeminiException;

    EntityRecord createOrUpdateEntityRecord(EntityRecord entityRecord, Transaction transaction) throws GeminiException;

    int updateEntityRecordsMatchingFilter(Entity entity,
                                          Collection<EntityRecord.EntityFieldValue> filterFieldValueType,
                                          Collection<EntityRecord.EntityFieldValue> updateWith,
                                          Transaction transaction) throws GeminiException;


}
