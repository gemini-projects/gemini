package it.at7.gemini.core;

import it.at7.gemini.conf.DynamicSchema;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.events.EventManager;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.*;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.EntityRef;
import it.at7.gemini.schema.FieldRef;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static it.at7.gemini.conf.State.PROVIDED_CLASSPATH_RECORDS_HANDLED;

@Service
public class EntityManagerImpl implements EntityManager, EntityManagerInit {
    public static final Set<String> CORE_ENTITIES = Set.of(EntityRef.NAME, FieldRef.NAME);

    private SchemaManager schemaManager;
    private TransactionManager transactionManager;
    private PersistenceEntityManager persistenceEntityManager;
    private StateManager stateManager;
    private GeminiConfigurationService configurationService;
    private EventManager eventManager;

    @Autowired
    public EntityManagerImpl(SchemaManager schemaManager, TransactionManager transactionManager, PersistenceEntityManager persistenceEntityManager, StateManager stateManager, GeminiConfigurationService configurationService, EventManager eventManager) {
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.persistenceEntityManager = persistenceEntityManager;
        this.stateManager = stateManager;
        this.configurationService = configurationService;
        this.eventManager = eventManager;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public PersistenceEntityManager getPersistenceEntityManager() {
        return persistenceEntityManager;
    }

    @Override
    public Collection<Entity> getAllEntities() {
        return schemaManager.getAllEntities();
    }

    @Override
    @Nullable
    public Entity getEntity(String entity) {
        return schemaManager.getEntity(entity);
    }


    @Override
    public EntityRecord putIfAbsent(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkEnabledState();
        checkDynamicSchema(record);
        checkEntity(record.getEntity());
        Optional<EntityRecord> rec = persistenceEntityManager.getEntityRecordByLogicalKey(record, transaction);
        if (!rec.isPresent()) {
            // can insert the entity record
            return createNewEntityRecord(record, entityOperationContext, transaction);
        }
        throw EntityRecordException.MULTIPLE_LK_FOUND(record);
    }

    @Override
    public EntityRecord putOrUpdate(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkEnabledState();
        checkDynamicSchema(record);
        Entity entity = record.getEntity();
        notAllowedOnClosedDomainEntity(entity);
        if (entity.isOneRecord())
            return updateOneRecordEntity(record, entityOperationContext, transaction);

        Optional<EntityRecord> rec = persistenceEntityManager.getEntityRecordByLogicalKey(record, transaction);
        if (!rec.isPresent()) {
            // can insert the entity record and return it
            return createNewEntityRecord(record, entityOperationContext, transaction);
        } else {
            EntityRecord recToUpdate = rec.get();
            return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, recToUpdate);
        }
    }

    @Override
    public EntityRecord update(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        if (record.hasID()) {
            return updateRecordByUsingID(record, entityOperationContext, transaction);
        }
        if (record.hasUUID()) {
            return update(record.getUUID(), record, entityOperationContext, transaction);
        }
        if (record.getEntity().isOneRecord()) {
            return updateOneRecordEntity(record, entityOperationContext, transaction);
        }
        return update(record.getLogicalKeyValue(), record, entityOperationContext, transaction);
    }

    @Override
    public EntityRecord update(Collection<? extends FieldValue> logicalKey, EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkEnabledState();
        checkDynamicSchema(record);
        notAllowedOnClosedDomainEntity(record.getEntity());
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByLogicalKey(record.getEntity(), logicalKey, transaction);
        if (persistedRecordOpt.isPresent()) {
            // can update
            EntityRecord persistedRecord = persistedRecordOpt.get();
            return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, persistedRecord);
        }
        throw EntityRecordException.LK_NOTFOUND(record.getEntity(), logicalKey);
    }

    @Override
    public EntityRecord update(UUID uuid, EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkEnabledState();
        checkDynamicSchema(record);
        notAllowedOnClosedDomainEntity(record.getEntity());
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

    private EntityRecord updateRecordByUsingID(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkEnabledState();
        checkDynamicSchema(record);
        notAllowedOnClosedDomainEntity(record.getEntity());
        return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, record);
    }

    private EntityRecord updateOneRecordEntity(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        assert record.getEntity().isOneRecord();
        EntityRecord singleEntityRecord = getOneRecordEntity(record.getEntity(), entityOperationContext, transaction);
        return updateRecordIfNeededHandlingEvents(record, entityOperationContext, transaction, singleEntityRecord);
    }

    @Override
    public EntityRecord delete(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkDynamicSchema(record.getEntity());
        checkEntity(record.getEntity());
        if (record.hasID()) {
            return deleteRecordHandlingEvents(transaction, entityOperationContext, record);
        }
        if (record.hasUUID()) {
            return delete(record.getEntity(), record.getUUID(), entityOperationContext, transaction);
        }
        return delete(record.getEntity(), record.getLogicalKeyValue(), entityOperationContext, transaction);
    }

    @Override
    public EntityRecord delete(Entity entity, Collection<? extends FieldValue> logicalKey, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkDynamicSchema(entity);
        checkEntity(entity);
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByLogicalKey(entity, logicalKey, transaction);
        if (persistedRecordOpt.isPresent()) {
            return deleteRecordHandlingEvents(transaction, entityOperationContext, persistedRecordOpt.get());
        }
        throw EntityRecordException.LK_NOTFOUND(entity, logicalKey);
    }

    @Override
    public EntityRecord delete(Entity entity, UUID uuid, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        checkDynamicSchema(entity);
        checkEntity(entity);
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getEntityRecordByUUID(entity, uuid, transaction);
        if (persistedRecordOpt.isPresent()) {
            return deleteRecordHandlingEvents(transaction, entityOperationContext, persistedRecordOpt.get());
        }
        throw EntityRecordException.UUID_NOTFOUND(entity, uuid);
    }

    private EntityRecord deleteRecordHandlingEvents(Transaction transaction, EntityOperationContext entityOperationContext, EntityRecord persistedRecord) throws GeminiException {
        // TODO handle entityOperationContextHandler
        // // TODO enable when dynamic schema -- handleDeleteSchemaCoreEntities(persistedRecord, transaction);
        handleDeleteResolution(persistedRecord, transaction); // TODO ? use entityOperationContext ??
        persistenceEntityManager.deleteEntityRecordByID(persistedRecord, transaction);
        return persistedRecord;
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
    public List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext) throws GeminiException {
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return getRecordsMatching(entity, filterContext, entityOperationContext, transaction);
        });
    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, FilterContext filterContext, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        // TODO add entityoperation context
        return persistenceEntityManager.getEntityRecordsMatching(entity, filterContext, transaction);
    }


    @Override
    public EntityRecord getOneRecordEntity(Entity entity, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        notAllowedyOnNOTSingleRecordEntity(entity);
        // TODO entityOperationContext
        return persistenceEntityManager.getEntityRecordSingleton(entity, transaction);
    }

    @Override
    public EntityRecord createOneRecordEntityRecord(Entity entity, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        notAllowedyOnNOTSingleRecordEntity(entity);
        return createNewEntityRecord(new EntityRecord(entity), entityOperationContext, transaction);
    }

    private EntityRecord createNewEntityRecord(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException {
        this.checkFrameworkEntitiesCreation(record);
        this.eventManager.beforeInsertFields(record, entityOperationContext, transaction);
        return persistenceEntityManager.createNewEntityRecord(record, transaction);
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


    private EntityRecord updateRecordIfNeededHandlingEvents(EntityRecord record, EntityOperationContext entityOperationContext, Transaction transaction, EntityRecord persistedRecord) throws GeminiException {
        eventManager.onUpdateFields(record, entityOperationContext, transaction);
        if (someRealUpdatedNeeded(record, persistedRecord)) {
            persistedRecord.update(record);
            return persistenceEntityManager.updateEntityRecordByID(persistedRecord, transaction);
        }
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

    @Override
    public EntityRecord get(Entity entity, Collection<? extends FieldValue> logicalKey, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> recordByLogicalKey = persistenceEntityManager.getEntityRecordByLogicalKey(entity, logicalKey, transaction);
        if (recordByLogicalKey.isPresent()) {
            return recordByLogicalKey.get();
        }
        throw EntityRecordException.LK_NOTFOUND(entity, logicalKey);
    }


    private EntityRecord get(Entity entity, UUID uuid, Transaction transaction) throws GeminiException {
        checkEnabledState();
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
                if (stateManager.getActualState().compareTo(State.INITIALIZED) >= 0 && CORE_ENTITIES.contains(name)) {
                    throw SchemaException.DYNAMIC_SCHEMA_NOT_ENABLED(name);
                }
        }
    }

    private void checkFrameworkEntitiesCreation(EntityRecord record) throws SchemaException {
        String entityName = record.getEntity().getName();
        State actualState = stateManager.getActualState();
        if (actualState.compareTo(PROVIDED_CLASSPATH_RECORDS_HANDLED) >= 0) {
            // cannot create core entity there
            if (CORE_ENTITIES.contains(entityName))
                throw SchemaException.FRAMEWORK_SCHEMA_RECORDS_NOT_MODIFIABLE_THERE(actualState.name());
        }
    }

    private void checkEntity(Entity entity) throws EntityException {
        notAllowedyOnSingleRecordEntity(entity);
        notAllowedOnClosedDomainEntity(entity);
    }

    private void notAllowedOnClosedDomainEntity(Entity entity) throws EntityException {
        if (entity.isClosedDomain()) {
            throw EntityException.API_NOT_ALLOWED_ON_CLOSED_DOMAIN(entity.getName());
        }
    }

    private void notAllowedyOnSingleRecordEntity(Entity entity) throws EntityException {
        if (entity.isOneRecord())
            throw EntityException.API_NOT_ALLOWED_ON_ONEREC(entity.getName());
    }

    private void notAllowedyOnNOTSingleRecordEntity(Entity entity) throws EntityException {
        if (!entity.isOneRecord())
            throw EntityException.API_NOT_ALLOWED_ON_NOT_ONEREC(entity.getName());
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
