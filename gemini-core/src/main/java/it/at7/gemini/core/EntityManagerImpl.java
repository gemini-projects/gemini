package it.at7.gemini.core;

import it.at7.gemini.conf.DynamicSchema;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.events.EventManager;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.InvalidStateException;
import it.at7.gemini.exceptions.SchemaException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.EntityRef;
import it.at7.gemini.schema.FieldRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EntityManagerImpl implements EntityManager {

    private SchemaManager schemaManager;
    private TransactionManager transactionManager;
    private PersistenceEntityManager persistenceEntityManager;
    private StateManager stateManager;
    private ConfigurationService configurationService;
    private EventManager eventManager;

    @Autowired
    public EntityManagerImpl(SchemaManager schemaManager, TransactionManager transactionManager, PersistenceEntityManager persistenceEntityManager, StateManager stateManager, ConfigurationService configurationService, EventManager eventManager) {
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.persistenceEntityManager = persistenceEntityManager;
        this.stateManager = stateManager;
        this.configurationService = configurationService;
        this.eventManager = eventManager;
    }

    @Override
    public Collection<Entity> getAllEntities() {
        return schemaManager.getAllEntities();
    }

    @Override
    public Entity getEntity(String entity) {
        return schemaManager.getEntity(entity);
    }

    @Override
    public EntityRecord putIfAbsent(EntityRecord rec) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return putIfAbsent(rec, transaction);
        });
    }

    @Override
    public Collection<EntityRecord> putIfAbsent(Collection<EntityRecord> recs) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            Collection<EntityRecord> ret = new ArrayList<>();
            for (EntityRecord rec : recs) {
                ret.add(putIfAbsent(rec, transaction));
            }
            return ret;
        });
    }

    @Override
    public EntityRecord putOrUpdate(EntityRecord record) throws GeminiException {
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return putOrUpdate(record, transaction);
        });
    }

    @Override
    public EntityRecord update(EntityRecord rec, Collection<? extends FieldValue> logicalKey) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return update(rec, logicalKey, EntityOperationContext.EMPTY, transaction);
        });
    }

    @Override
    public EntityRecord update(EntityRecord rec, UUID uuid) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return update(rec, uuid, EntityOperationContext.EMPTY, transaction);
        });
    }

    @Override
    public EntityRecord delete(Entity entity, Collection<? extends FieldValue> logicalKey) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return delete(entity, logicalKey, transaction);
        });
    }


    @Override
    public EntityRecord delete(Entity entity, UUID uuid) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return delete(entity, uuid, transaction);
        });
    }

    @Override
    public EntityRecord get(Entity entity, Collection<? extends FieldValue> logicalKey) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return get(entity, logicalKey, transaction);
        });
    }

    @Override
    public EntityRecord get(Entity entity, UUID uuid) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return get(entity, uuid, transaction);
        });
    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType) throws GeminiException {
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return getRecordsMatching(entity, filterFielValueType, transaction);
        });
    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, Set<FieldValue> filterFielValueType, Transaction transaction) throws GeminiException {
        return persistenceEntityManager.getEntityRecordsMatching(entity, filterFielValueType, transaction);

    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext) throws GeminiException {
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return getRecordsMatching(entity, filterContext, transaction);
        });
    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, Transaction transaction) throws GeminiException {
        return persistenceEntityManager.getEntityRecordsMatching(entity, filterContext, transaction);
    }


    private EntityRecord putIfAbsent(EntityRecord record, Transaction transaction) throws GeminiException {
        return putIfAbsent(record, EntityOperationContext.EMPTY, transaction);
    }

    private EntityRecord putIfAbsent(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkDynamicSchema(record);
        Optional<EntityRecord> rec = persistenceEntityManager.getEntityRecordByLogicalKey(record, transaction);
        if (!rec.isPresent()) {
            // can insert the entity record
            return createNewEntityRecord(record, entityOperationContext, transaction);
        }
        throw EntityRecordException.MULTIPLE_LK_FOUND(record);
    }

    private EntityRecord createNewEntityRecord(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        this.eventManager.beforeInsertFields(record, entityOperationContext, transaction);
        return persistenceEntityManager.createNewEntityRecord(record, transaction);
    }

    public EntityRecord putOrUpdate(EntityRecord record, Transaction transaction) throws GeminiException {
        return putOrUpdate(record, EntityOperationContext.EMPTY, transaction);
    }

    public EntityRecord putOrUpdate(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkEnabledState();
        checkDynamicSchema(record);
        Optional<EntityRecord> rec = persistenceEntityManager.getEntityRecordByLogicalKey(record, transaction);
        if (!rec.isPresent()) {
            // can insert the entity record
            return createNewEntityRecord(record, entityOperationContext, transaction);
        } else {
            EntityRecord persistedRecord = rec.get();
            return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, persistedRecord);
        }
    }

    private boolean someRealUpdatedNeeded(EntityRecord record, EntityRecord persistedRecord) {
        for (EntityFieldValue ev : record.getOnlyModifiedEntityFieldValue()) {
            EntityField entityField = ev.getEntityField();
            Object persistedValue = persistedRecord.get(entityField);
            if (!ev.fieldValueEquals(persistedValue)) {
                return true;
            }
        }
        return false;
    }


    private EntityRecord update(EntityRecord record, Collection<? extends FieldValue> logicalKey, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkDynamicSchema(record);
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByLogicalKey(record.getEntity(), logicalKey, transaction);
        if (persistedRecordOpt.isPresent()) {
            // can update
            EntityRecord persistedRecord = persistedRecordOpt.get();
            return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, persistedRecord);
        }
        throw EntityRecordException.LK_NOTFOUND(record.getEntity(), logicalKey);
    }

    private EntityRecord update(EntityRecord record, UUID uuid, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkDynamicSchema(record);
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByUUID(record.getEntity(), uuid, transaction);
        if (persistedRecordOpt.isPresent()) {
            EntityRecord persistedRecord = persistedRecordOpt.get();
            Optional<EntityRecord> lkRecord = persistenceEntityManager.getEntityRecordByLogicalKey(record, transaction);
            if (lkRecord.isPresent()) {
                // the uuid / id must be the same.. otherwise we lack the logical key uniqueness
                EntityRecord lkEntityRecord = lkRecord.get();
                assert persistedRecord.getID() != null && lkEntityRecord.getID() != null;
                if (!persistedRecord.getID().equals(lkEntityRecord.getID())) {
                    throw EntityRecordException.MULTIPLE_LK_FOUND(record);
                }
            }
            // can update
            return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, persistedRecord);
        }
        throw EntityRecordException.UUID_NOTFOUND(record.getEntity(), uuid);
    }

    private EntityRecord updateRecordIfNeededHandlingEvents(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction, EntityRecord persistedRecord) throws GeminiException {
        eventManager.onUpdateFields(record, entityOperationContext, transaction);
        if (someRealUpdatedNeeded(record, persistedRecord)) {
            persistedRecord.update(record);
            return persistenceEntityManager.updateEntityRecordByID(persistedRecord, transaction);
        }
        return persistedRecord;
    }

    private EntityRecord delete(Entity entity, Collection<? extends FieldValue> logicalKey, Transaction transaction) throws GeminiException {
        checkDynamicSchema(entity);
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByLogicalKey(entity, logicalKey, transaction);
        if (persistedRecordOpt.isPresent()) {
            return deleteInner(transaction, persistedRecordOpt.get());
        }
        throw EntityRecordException.LK_NOTFOUND(entity, logicalKey);
    }

    private EntityRecord delete(Entity entity, UUID uuid, Transaction transaction) throws GeminiException {
        checkDynamicSchema(entity);
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByUUID(entity, uuid, transaction);
        if (persistedRecordOpt.isPresent()) {
            return deleteInner(transaction, persistedRecordOpt.get());
        }
        throw EntityRecordException.UUID_NOTFOUND(entity, uuid);
    }

    private EntityRecord deleteInner(Transaction transaction, EntityRecord persistedRecord) throws GeminiException {
        // // TODO enable when dynamic schema -- handleDeleteSchemaCoreEntities(persistedRecord, transaction);
        handleDeleteResolution(persistedRecord, transaction);
        persistenceEntityManager.deleteEntityRecordByID(persistedRecord, transaction);
        return persistedRecord;
    }

    /* TODO on dynamic schema
    private boolean checkFieldisNew(EntityField fieldFromRecord) {
        Entity entity = fieldFromRecord.getEntity();
        Set<EntityField> schemaEntityFields = entity.getDataEntityFields();
        return schemaEntityFields.stream().noneMatch(f -> f.getName().toLowerCase().equals(fieldFromRecord.getName().toLowerCase()));
    }

    private Entity getEntityFromRecord(EntityRecord record) {
        String name = record.get(EntityRef.FIELDS.NAME, String.class).toUpperCase();
        return schemaManager.getEntity(name);
    }

    private Entity createNewEntity(EntityRecord record) throws ModuleException {
        String name = record.get("name");
        String module = record.getFieldOrSetDefault("module", "RUNTIME");
        Module runtimeModule = schemaManager.getModule(module.toUpperCase());
        if (runtimeModule == null) {
            throw ModuleException.NOT_FOUND(module);
        }
        if (!runtimeModule.editable()) {
            throw ModuleException.NOT_EDITABLE_MODULE(module);
        }
        return new EntityBuilder(name, runtimeModule).build();
    }
    */

    private void handleDeleteResolution(EntityRecord entityRecord, Transaction transaction) throws GeminiException {
        ResolutionExecutor resolutionExecutor = ResolutionExecutor.forDelete(entityRecord, persistenceEntityManager, schemaManager, transaction);
        resolutionExecutor.run();
    }


    private EntityRecord get(Entity entity, Collection<? extends FieldValue> logicalKey, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> recordByLogicalKey = persistenceEntityManager.getEntityRecordByLogicalKey(entity, logicalKey, transaction);
        if (recordByLogicalKey.isPresent()) {
            return recordByLogicalKey.get();
        }
        throw EntityRecordException.LK_NOTFOUND(entity, logicalKey);
    }

    private EntityRecord get(Entity entity, UUID uuid, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> uuidPersisted = persistenceEntityManager.getEntityRecordByUUID(entity, uuid, transaction);
        if (uuidPersisted.isPresent()) {
            return uuidPersisted.get();
        }
        throw EntityRecordException.UUID_NOTFOUND(entity, uuid);
    }

    private void checkEnabledState() throws InvalidStateException {
        State actualState = stateManager.getActualState();
        if (actualState.compareTo(State.SCHEMA_STORAGE_INITIALIZED) < 0) {
            throw InvalidStateException.STATE_LESS_THAN(actualState, State.SCHEMA_STORAGE_INITIALIZED);
        }
    }

    private void checkDynamicSchema(EntityRecord record) throws SchemaException {
        checkDynamicSchema(record.getEntity());
    }

    private void checkDynamicSchema(Entity entity) throws SchemaException {
        DynamicSchema dynamicSchema = configurationService.getDynamicSchema();
        switch (dynamicSchema) {
            case ALL:
                break;
            case DISABLED:
                String name = entity.getName();
                Set<String> core_entities = Set.of(EntityRef.NAME, FieldRef.NAME);
                if (stateManager.getActualState().compareTo(State.INITIALIZED) >= 0 && core_entities.contains(name)) {
                    throw SchemaException.DYNAMIC_SCHEMA_NOT_ENABLED(name);
                }
        }
    }

     /* // TODO dynamic entity record
    private void handleInsertSchemaCoreEntities(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();
        switch (entity.getName()) {
            case ENTITY:
                Entity entityFromRecord = getEntityFromRecord(record);
                if (entityFromRecord != null) { // it must be inexistent entity
                    throw EntityException.ENTITY_FOUND(entityFromRecord);
                }
                Entity newEntity = createNewEntity(record);
                schemaManager.addNewRuntimeEntity(newEntity, transaction);
                break;
            case FIELD:
                EntityField fieldFromRecord = getEntityFieldFromRecord(record);
                if (!checkFieldisNew(fieldFromRecord)) {
                    throw EntityFieldException.ENTITYFIELD_ALREADY_FOUND(fieldFromRecord);
                }
                schemaManager.addNewRuntimeEntityField(fieldFromRecord, transaction);
        }
    }

    private void handleDeleteSchemaCoreEntities(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();
        switch (entity.getName()) {
            case ENTITY:
                Entity entityFromRecord = getEntityFromRecord(record);
                if (entityFromRecord == null) { // it must be inexistent entity
                    throw EntityException.ENTITY_NOT_FOUND(entityFromRecord.getName());
                }
                schemaManager.deleteRuntimeEntity(entityFromRecord, transaction);
                break;
            case FIELD:
                EntityField fieldFromRecord = getEntityFieldFromRecord(record);
                if (!checkFieldisNew(fieldFromRecord)) {
                    throw EntityFieldException.ENTITYFIELD_ALREADY_FOUND(fieldFromRecord);
                }
                schemaManager.deleteRuntimeEntityField(fieldFromRecord, transaction);
        }
    }

    private EntityField getEntityFieldFromRecord(EntityRecord record) throws FieldException {
        String name = record.getRequiredField("name");
        EntityReferenceRecord entity = record.getRequiredField("entity");
        String entityName = entity.getLogicalKeyRecord().get("name");
        Entity entityObj = schemaManager.getEntity(entityName);
        boolean isLogicalKey = record.getFieldOrDefault("isLogicalKey", false);
        String type = record.getRequiredField("type");
        FieldType fieldType = FieldType.valueOf(type);
        String entityRef = null;
        if (fieldType.equals(FieldType.ENTITY_REF)) {
            EntityReferenceRecord entityRefPK = record.get("entityRef");
            assert entityRefPK != null;
            entityRef = entityRefPK.getLogicalKeyRecord().getRequiredField("name");
        }
        EntityFieldBuilder entityFieldBuilder = new EntityFieldBuilder(fieldType, name, isLogicalKey, entityRef, scope);
        entityFieldBuilder.setEntity(entityObj);
        return entityFieldBuilder.build();
    }
    */
}
