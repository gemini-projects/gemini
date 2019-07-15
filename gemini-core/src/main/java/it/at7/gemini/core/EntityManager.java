package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface EntityManager {

    // === UTILITY METHODS === //

    /**
     * Get all the entities handled by Gemini
     */
    Collection<Entity> getAllEntities();

    /**
     * Return the transaction manager to implement default interface methods
     */
    TransactionManager getTransactionManager();

    /**
     * Get the entity by its name or return null
     *
     * @param entity name
     * @return entity object
     */
    @Nullable
    Entity getEntity(String entity);
    // ===================== //


    /**
     * Create entity record if absent or throws Exception if it already exists (accordingly to its logical Key).
     * Record is inserted is a new fresh Transaction returned by getTransactionManager() and using a default empty
     * {@link EntityResolutionContext}
     *
     * @param record record to add
     * @return the inserted EntityRecord
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    default EntityRecord putIfAbsent(EntityRecord record) throws GeminiException {
        return putIfAbsent(record, EntityOperationContext.EMPTY);
    }

    /**
     * Create entity record if absent or throws Exception if it already exists (accordingly to its logical Key).
     * Record is inserted is a new fresh Transaction returned by getTransactionManager() and using a default empty
     * {@link EntityResolutionContext}
     *
     * @param record record to add
     * @return the inserted EntityRecord
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    default EntityRecord putIfAbsent(EntityRecord record, EntityOperationContext operationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return putIfAbsent(record, operationContext, transaction);
        });
    }

    /**
     * Create entity record if absent or throws Exception if it already exists (accordingly to its logical Key).
     * Record is inserted is the provided transaction using the default empty {@link EntityResolutionContext}
     *
     * @param record      record to add
     * @param transaction the transaction to be used to insert the record
     * @return the inserted EntityRecord
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    default EntityRecord putIfAbsent(EntityRecord record, Transaction transaction) throws GeminiException {
        return putIfAbsent(record, EntityOperationContext.EMPTY, transaction);
    }

    /**
     * Create entity record if absent or throws Exception if it already exists (accordingly to its logical Key).
     * Record is inserted is the provided transaction using the provided {@link EntityResolutionContext}
     *
     * @param record                 record to add
     * @param transaction            the transaction to be used to insert the record
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return the inserted EntityRecord
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    EntityRecord putIfAbsent(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;


    /**
     * Create all the entityRecord or throws Exception if at least one of them already exists. All the records are
     * inserted in the same fresh transaction.
     *
     * @param records EntityRecords to insert at all
     * @return all the inserted Entity Records
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    default Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> records) throws GeminiException {
        return putIfAbsent(records, EntityOperationContext.EMPTY);
    }

    /**
     * Create all the entityRecord or throws Exception if at least one of them already exists. All the records are
     * inserted in the same fresh transaction with the provided entityOperatioContext
     *
     * @param records                EntityRecords to insert at all
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return all the inserted Entity Records
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    default Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> records, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return putIfAbsent(records, entityOperationContext, transaction);
        });
    }

    /**
     * Create all the entityRecord or throws Exception if at least one of them already exists. All the records are
     * inserted in the provided transaction with the provided entityOperatioContext
     *
     * @param records                EntityRecords to insert at all
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @param transaction
     * @return all the inserted Entity Records
     * @throws GeminiException {@link it.at7.gemini.exceptions.EntityRecordException}
     */
    default Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> records, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        Collection<EntityRecord> ret = new ArrayList<>();
        for (EntityRecord rec : records) {
            ret.add(putIfAbsent(rec, entityOperationContext, transaction));
        }
        return ret;
    }

    EntityRecord putOrUpdate(EntityRecord rec) throws GeminiException;

    EntityRecord putOrUpdate(EntityRecord rec, Transaction transaction) throws GeminiException;

    EntityRecord putOrUpdate(EntityRecord rec, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    default EntityRecord update(EntityRecord rec) throws GeminiException {
        return update(rec, rec.getLogicalKeyValue());
    }

    EntityRecord update(EntityRecord rec, Collection<? extends FieldValue> logicalKey) throws GeminiException;

    EntityRecord update(EntityRecord rec, UUID uuid) throws GeminiException;

    default EntityRecord delete(EntityRecord entityRecord) throws GeminiException {
        return delete(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    EntityRecord delete(Entity e, Collection<? extends FieldValue> logicalKey) throws GeminiException;

    EntityRecord delete(Entity e, UUID uuid) throws GeminiException;

    default EntityRecord get(EntityRecord entityRecord) throws GeminiException {
        return get(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
    }

    EntityRecord get(Entity e, Collection<? extends FieldValue> logicalKey) throws GeminiException;

    EntityRecord get(Entity e, UUID uuid) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, DynamicRecord searchRecord) throws GeminiException {
        assert searchRecord != null;
        return getRecordsMatching(entity, searchRecord.getFieldValues());
    }

    default List<EntityRecord> getRecordsMatching(Entity entity, String field, Object value) throws GeminiException {
        EntityField entityField = entity.getField(field);
        FieldValue fieldValue = FieldValue.create(entityField, value);
        return getRecordsMatching(entity, Set.of(fieldValue));
    }

    List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType, Transaction transaction) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, Transaction transaction) throws GeminiException;
}
