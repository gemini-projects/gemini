package it.at7.gemini.core.persistence;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.FilterRequest;
import it.at7.gemini.core.Record;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PersistenceEntityManager {

    List<EntityRecord> getRecordsMatching(Entity entity, Collection<? extends Record.FieldValue> filterFieldValueType, Transaction transaction) throws GeminiException;


    List<EntityRecord> getRecordsMatching(Entity entity, FilterRequest filterRequest, Transaction transaction) throws GeminiException;

    default Optional<EntityRecord> getRecordByLogicalKey(Entity entity, Record logicalKeyRecord, Transaction transaction) throws GeminiException {
        return getRecordByLogicalKey(entity, logicalKeyRecord.getFieldValues(), transaction);
    }

    Optional<EntityRecord> getRecordByLogicalKey(Entity entity, Collection<? extends Record.FieldValue> logicalKey, Transaction transaction) throws GeminiException;

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
