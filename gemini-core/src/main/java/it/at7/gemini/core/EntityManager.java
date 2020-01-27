package it.at7.gemini.core;

import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.EntityException;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.EntityRecordException.LkNotFoundException;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface EntityManager {

    /**
     * @return Get all the entities handled by Gemini
     */
    Collection<Entity> getAllEntities();

    /**
     * @return the transaction manager (useful to implement default interface methods)
     */
    TransactionManager getTransactionManager();

    /**
     * @return the persistence entity manager (useful to implement default interface methods)
     */
    PersistenceEntityManager getPersistenceEntityManager();

    /**
     * Get the entity by its name or return null
     *
     * @param entity name
     * @return entity object
     */
    @Nullable
    Entity getEntity(String entity);

    /**
     * Create a new empty Entity Record
     *
     * @param entity target entity
     * @return the new entity Record
     */
    default EntityRecord getNewEntityRecord(String entity) {
        return new EntityRecord(getEntity(entity));
    }

    /**
     * Create entity record if absent or throws Exception if it already exists (accordingly to its logical Key).
     * Record is inserted is a new fresh Transaction returned by getTransactionManager() and using a default empty
     * {@link EntityOperationContext}
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
     * {@link EntityOperationContext}
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
     * Record is inserted is the provided transaction using the default empty {@link EntityOperationContext}
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
     * Record is inserted is the provided transaction using the provided {@link EntityOperationContext}
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
     * inserted in the same fresh transaction withRecord the provided entityOperatioContext
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
     * inserted in the provided transaction withRecord the provided entityOperatioContext
     *
     * @param records                EntityRecords to insert at all
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @param transaction
     * @return all the inserted Entity Records
     * @throws GeminiException in general {@link it.at7.gemini.exceptions.EntityRecordException}: if the record (by lk) already exists
     */
    default Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> records, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        Collection<EntityRecord> ret = new ArrayList<>();
        for (EntityRecord rec : records) {
            ret.add(putIfAbsent(rec, entityOperationContext, transaction));
        }
        return ret;
    }

    /**
     * Create entity record if absent or update if it already exists (accordingly to its logical Key).
     * Record is inserted in a new fresh transaction using the default empty {@link EntityOperationContext}
     *
     * @param record record to add
     * @return the inserted EntityRecord
     * @throws GeminiException if something goes wrong withRecord persistence operations
     */
    default EntityRecord putOrUpdate(EntityRecord record) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return putOrUpdate(record, transaction);
        });
    }

    /**
     * Create entity record if absent or update if it already exists (accordingly to its logical Key).
     * Record is inserted is the provided transaction using the default empty {@link EntityOperationContext}
     *
     * @param record      record to add
     * @param transaction the transaction to be used to insert the record
     * @return the inserted EntityRecord
     * @throws GeminiException if something goes wrong withRecord persistence operations
     */
    default EntityRecord putOrUpdate(EntityRecord record, Transaction transaction) throws GeminiException {
        return putOrUpdate(record, EntityOperationContext.EMPTY, transaction);
    }

    /**
     * Create entity record if absent or update if it already exists (accordingly to its logical Key).
     * Record is inserted is the provided transaction using the provided {@link EntityOperationContext}
     *
     * @param record                 record to add
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @param transaction            the transaction to be used to insert the record
     * @return the inserted EntityRecord
     * @throws GeminiException if something goes wrong withRecord persistence operations
     */
    EntityRecord putOrUpdate(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    /**
     * Update the provided record in a new fresh transaction and using the default EnittyOperationContext.
     *
     * @param record record to update. If the record contains the persistence ID the implementation must allow
     *               the update of logical key fields. Otherwise the record is firts of all retrieved (from persistence
     *               manager) by its logical key and then updated.
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord update(EntityRecord record) throws GeminiException {
        return update(record, EntityOperationContext.EMPTY);
    }

    /**
     * Update the provided record in a new fresh transaction and using the provided EnittyOperationContext.
     *
     * @param record                 record to update. If the record contains the persistence ID the implementation must allow
     *                               the update of logical key fields. Otherwise the record is firts of all retrieved (from persistence
     *                               manager) by its logical key and then updated.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord update(EntityRecord record, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return update(record, entityOperationContext, transaction);
        });
    }

    /**
     * Update the entity record retrieved by the logicalKey in a new fresh transaction and using default EnittyOperationContext.
     *
     * @param logicalKey fields to retrieve the persisted entity record to update
     * @param record     record to be used to ovveride the persisted entity record returned by logical key.
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord update(Collection<? extends FieldValue> logicalKey, EntityRecord record) throws GeminiException {
        return update(logicalKey, record, EntityOperationContext.EMPTY);
    }

    /**
     * Update the entity record retrieved by the logicalKey in a new fresh transaction and using the provided EnittyOperationContext.
     *
     * @param logicalKey             fields to retrieve the persisted entity record to update
     * @param record                 record to be used to ovveride the persisted entity record returned by logical key.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord update(Collection<? extends FieldValue> logicalKey, EntityRecord record, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return update(logicalKey, record, entityOperationContext, transaction);
        });
    }

    /**
     * Update the entity record retrieved by the UUID in a new fresh transaction and using the provided EnittyOperationContext.
     *
     * @param uuid                   uuid
     * @param record                 record to be used to ovveride the persisted entity record returned by logical key.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord update(UUID uuid, EntityRecord record, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return update(uuid, record, entityOperationContext, transaction);
        });
    }

    /**
     * Update the provided record in the provided transaction and using the provided EnittyOperationContext.
     *
     * @param record                 record to update. If the record contains the persistence ID the implementation must allow
     *                               the update of logical key fields. Otherwise the record is firts of all retrieved (from persistence
     *                               manager) by its logical key and then updated.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @param transaction            the transaction to be used to update the record
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    EntityRecord update(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    /**
     * Update the entity record retrieved by the logicalKey in the provided transaction and using the provided EnittyOperationContext.
     *
     * @param logicalKey             fields to retrieve the persisted entity record to update
     * @param record                 record to be used to ovveride the persisted entity record returned by logical key.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    EntityRecord update(Collection<? extends FieldValue> logicalKey, EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    /**
     * Update the entity record retrieved by the UUID in the provided transaction and using the provided EnittyOperationContext.
     *
     * @param uuid                   uuid
     * @param record                 record to be used to ovveride the persisted entity record returned by logical key.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    EntityRecord update(UUID uuid, EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    /**
     * Delete the provided record in a new fresh transaction and using the default EnittyOperationContext.
     *
     * @param record record to delete. If the record contains the persistence ID the implementation must delete by it
     *               Otherwise the record is firts of all retrieved (from persistence manager) by its logical key and then updated.
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord delete(EntityRecord record) throws GeminiException {
        return delete(record, EntityOperationContext.EMPTY);
    }

    /**
     * Delete the provided record in a new fresh transaction and using provided EntityOperationContext
     *
     * @param record                 record to delete. If the record contains the persistence ID the implementation must delete by it
     *                               Otherwise the record is firts of all retrieved (from persistence manager) by its logical key and then updated.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord delete(EntityRecord record, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return delete(record, entityOperationContext, transaction);
        });
    }

    /**
     * Delete an entity record by its UUID in a new fresh transaction and using provided EntityOperationContext
     *
     * @param entity                 target Entity
     * @param uuid                   entity record identifier
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord delete(Entity entity, UUID uuid, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return delete(entity, uuid, entityOperationContext, transaction);
        });
    }

    /**
     * Delete an entity record by its logical key in a new fresh transaction and using the default EntityOperationContext
     *
     * @param entity     target Entity
     * @param logicalKey entity record identifier (by its logical key)
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord delete(Entity entity, Collection<? extends FieldValue> logicalKey) throws GeminiException {
        return delete(entity, logicalKey, EntityOperationContext.EMPTY);
    }

    /**
     * Delete an entity record by its logical key in a new fresh transaction and using the provided EntityOperationContext
     *
     * @param entity                 target Entity
     * @param logicalKey             entity record identifier (by its logical key)
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    default EntityRecord delete(Entity entity, Collection<? extends FieldValue> logicalKey, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return delete(entity, logicalKey, entityOperationContext, transaction);
        });
    }

    /**
     * Delete the provided record in the provided transaction and using the provided EnittyOperationContext.
     *
     * @param record                 record to delete. If the record contains the persistence ID the implementation must allow
     *                               the update of logical key fields. Otherwise the record is first of all retrieved (from persistence
     *                               manager) by its logical key and then deleted.
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @param transaction            the transaction to be used to update the record
     * @return record updated
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    EntityRecord delete(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;


    /**
     * Delete an entity record by its UUID in the provided transaction and using provided EntityOperationContext
     *
     * @param entity                 target Entity
     * @param uuid                   entity record identifier
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    EntityRecord delete(Entity entity, UUID uuid, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    /**
     * Delete an entity record by its logical key the provided transaction and using the provided EntityOperationContext
     *
     * @param entity                 target Entity
     * @param logicalKey             entity record identifier (by its logical key)
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return record deleted
     * @throws GeminiException if the record is not found or something goes wrong withRecord persistence operations
     */
    EntityRecord delete(Entity entity, Collection<? extends FieldValue> logicalKey, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;


    /**
     * Allowed only for One Record Entities (Singleton). Get the singleton record for the entity.
     *
     * @param entity target Entity (must be a one record entity)
     * @return the singleton record
     * @throws GeminiException if persistence error occurs or called on non oneRecord entities
     */
    default EntityRecord getOneRecordEntity(Entity entity) throws GeminiException {
        return getOneRecordEntity(entity, EntityOperationContext.EMPTY);
    }

    /**
     * Allowed only for One Record Entities (Singleton). Get the singleton record for the entity.
     *
     * @param entity                 target Entity (must be a one record entity)
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @return the singleton record
     * @throws GeminiException if persistence error occurs or called on non oneRecord entities
     */
    default EntityRecord getOneRecordEntity(Entity entity, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return getOneRecordEntity(entity, entityOperationContext, transaction);
        });
    }

    /**
     * Allowed only for One Record Entities (Singleton). Get the singleton record for the entity.
     *
     * @param entity                 target Entity (must be a one record entity)
     * @param entityOperationContext the operationContext to retrieve information and custom logic
     * @param transaction            the transaction to be used to update the record
     * @return the singleton record
     * @throws GeminiException if persistence error occurs or called on non oneRecord entities
     */
    EntityRecord getOneRecordEntity(Entity entity, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;


    // TODO from here improve documentation and add entityOperationContext

    default EntityRecord get(EntityRecord record) throws GeminiException {
        return get(record.getEntity(), record.getLogicalKeyValue());
    }

    default Optional<EntityRecord> getOptional(EntityRecord record, Transaction transaction) throws GeminiException {
        try {
            EntityRecord entityRecord = get(record.getEntity(), record.getLogicalKeyValue(), transaction);
            return Optional.of(entityRecord);
        } catch (GeminiException e) {
            if (e.is(EntityRecordException.Code.LK_NOTFOUND))
                return Optional.empty();
            throw e;
        }
    }

    default EntityRecord get(Entity entity, EntityReferenceRecord entityReferenceRecord, Transaction transaction) throws GeminiException {
        return get(entity, entityReferenceRecord.getLogicalKeyRecord().getFieldValues(), transaction);
    }

    default Optional<EntityRecord> getOptional(Entity entity, EntityReferenceRecord entityReferenceRecord, Transaction transaction) throws GeminiException {
        try {
            EntityRecord entityRecord = get(entity, entityReferenceRecord, transaction);
            return Optional.of(entityRecord);
        } catch (GeminiException e) {
            if (e.is(EntityRecordException.Code.LK_NOTFOUND))
                return Optional.empty();
            throw e;
        }
    }

    default EntityRecord get(Entity entity, Collection<? extends FieldValue> logicalKey) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return get(entity, logicalKey, transaction);
        });
    }

    /**
     * Get a recordy by its entity and its logical key
     *
     * @param entityName name of entity (case insensitive)
     * @param logicalKey logical key object for the specified entity
     * @return
     * @throws LkNotFoundException if the specified logical key is not found
     * @throws GeminiException     if some error generic errors occurs
     */
    default EntityRecord get(String entityName, Object logicalKey) throws LkNotFoundException, GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return get(entityName, logicalKey, transaction);
        });
    }

    default EntityRecord get(String entityName, Object logicalKey, Transaction transaction) throws GeminiException {
        return get(getEntity(entityName), logicalKey, transaction);
    }

    default EntityRecord get(Entity entity, Object lk, Transaction transaction) throws GeminiException {
        EntityReferenceRecord entityReferenceRecord = FieldConverters.logicalKeyFromObject(entity, lk);
        return get(entity, entityReferenceRecord, transaction);
    }

    EntityRecord get(Entity entity, Collection<? extends FieldValue> logicalKey, Transaction transaction) throws GeminiException;

    EntityRecord get(Entity e, UUID uuid) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, DynamicRecord searchRecord) throws GeminiException {
        assert searchRecord != null;
        return getRecordsMatching(entity, searchRecord.getFieldValues());
    }

    default List<EntityRecord> getRecordsMatching(String entity, String field, Object value) throws GeminiException {
        Entity realEntity = getEntity(entity);
        if (realEntity == null) {
            throw EntityException.ENTITY_NOT_FOUND(entity);
        }
        return getRecordsMatching(realEntity, field, value);
    }

    default List<EntityRecord> getRecordsMatching(Entity entity, String field, Object value) throws GeminiException {
        EntityField entityField = entity.getField(field);
        FieldValue fieldValue = FieldValue.create(entityField, value);
        return getRecordsMatching(entity, Set.of(fieldValue));
    }

    default List<EntityRecord> getRecordsMatching(Entity entity, String field, Object value, Transaction transaction) throws GeminiException {
        EntityField entityField = entity.getField(field);
        FieldValue fieldValue = FieldValue.create(entityField, value);
        return getRecordsMatching(entity, Set.of(fieldValue), transaction);
    }

    List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType) throws GeminiException;

    List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType, Transaction transaction) throws GeminiException;

    default List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext) throws GeminiException {
        return getRecordsMatching(entity, filterContext, EntityOperationContext.EMPTY);
    }

    default List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return getRecordsMatching(entity, filterContext, entityOperationContext, transaction);
        });
    }

    List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

    default long countRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext) throws GeminiException {
        return getTransactionManager().executeInSingleTrasaction(transaction -> {
            return countRecordsMatching(entity, filterContext, entityOperationContext, transaction);
        });
    }

    long countRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

}
