package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.*;
import it.at7.gemini.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static it.at7.gemini.schema.Entity.NAME;

@Service
public class EntityManagerImpl implements EntityManager {
    public static final String ENTITY = "ENTITY";
    public static final String FIELD = "FIELD";

    private SchemaManager schemaManager;
    private TransactionManager transactionManager;
    private PersistenceEntityManager persistenceEntityManager;
    private StateManager stateManager;

    @Autowired
    public EntityManagerImpl(SchemaManager schemaManager, TransactionManager transactionManager, PersistenceEntityManager persistenceEntityManager, StateManager stateManager) {
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.persistenceEntityManager = persistenceEntityManager;
        this.stateManager = stateManager;
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
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return putOrUpdate(record, transaction);
        });
    }

    @Override
    public EntityRecord update(EntityRecord rec, Collection<? extends Record.FieldValue> logicalKey) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return update(rec, logicalKey, transaction);
        });
    }

    @Override
    public EntityRecord delete(Entity entity, Collection<? extends Record.FieldValue> logicalKey) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return delete(entity, logicalKey, transaction);
        });
    }

    @Override
    public EntityRecord get(Entity entity, Collection<? extends Record.FieldValue> logicalKey) throws GeminiException {
        checkEnabledState();
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return get(entity, logicalKey, transaction);
        });
    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, Set<Record.FieldValue> filterFielValueType) throws GeminiException {
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return getRecordsMatching(entity, filterFielValueType, transaction);
        });
    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, Set<Record.FieldValue> filterFielValueType, Transaction transaction) throws GeminiException {
        return persistenceEntityManager.getRecordsMatching(entity, filterFielValueType, transaction);

    }

    @Override
    public List<EntityRecord> getRecordsMatching(Entity entity, FilterRequest filterRequest) throws GeminiException {
        return transactionManager.executeInSingleTrasaction(transaction -> {
            return persistenceEntityManager.getRecordsMatching(entity, filterRequest, transaction);
        });
    }


    private EntityRecord putIfAbsent(EntityRecord record, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> rec = persistenceEntityManager.getRecordByLogicalKey(record, transaction);
        if (!rec.isPresent()) {
            handleInsertSchemaCoreEntities(record, transaction);
            // can insert the entity record
            return persistenceEntityManager.saveNewEntityRecord(record, transaction);
        }
        throw EntityRecordException.MULTIPLE_LK_FOUND(record);
    }

    private EntityRecord putOrUpdate(EntityRecord record, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> rec = persistenceEntityManager.getRecordByLogicalKey(record, transaction);
        if (!rec.isPresent()) {
            handleInsertSchemaCoreEntities(record, transaction);
            // can insert the entity record
            return persistenceEntityManager.saveNewEntityRecord(record, transaction);
        } else {
            EntityRecord persistedRecord = rec.get();
            persistedRecord.update(record);
            return persistenceEntityManager.updateEntityRecord(persistedRecord, transaction);
        }
    }

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
                    throw EntityException.ENTITY_FIELD_FOUND(fieldFromRecord);
                }
                schemaManager.addNewRuntimeEntityField(fieldFromRecord, transaction);
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
        EntityFieldBuilder entityFieldBuilder = new EntityFieldBuilder(fieldType, name, isLogicalKey, entityRef);
        entityFieldBuilder.setEntity(entityObj);
        return entityFieldBuilder.build();
    }

    private boolean checkFieldisNew(EntityField fieldFromRecord) {
        Entity entity = fieldFromRecord.getEntity();
        Set<EntityField> schemaEntityFields = entity.getSchemaEntityFields();
        return schemaEntityFields.stream().noneMatch(f -> f.getName().toLowerCase().equals(fieldFromRecord.getName().toLowerCase()));
    }

    private Entity getEntityFromRecord(EntityRecord record) {
        String name = record.get(NAME);
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

    private EntityRecord update(EntityRecord record, Collection<? extends Record.FieldValue> logicalKey, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getRecordByLogicalKey(record.getEntity(), logicalKey, transaction);
        if (persistedRecordOpt.isPresent()) {
            // can update
            EntityRecord persistedRecord = persistedRecordOpt.get();
            persistedRecord.update(record);
            return persistenceEntityManager.updateEntityRecord(persistedRecord, transaction);
        }
        throw EntityRecordException.LK_NOTFOUND(record.getEntity(), logicalKey);
    }

    private EntityRecord delete(Entity entity, Collection<? extends Record.FieldValue> logicalKey, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> persistedRecordOpt = persistenceEntityManager.getRecordByLogicalKey(entity, logicalKey, transaction);
        if (persistedRecordOpt.isPresent()) {
            EntityRecord persistedRecord = persistedRecordOpt.get();
            handleDeleteSchemaCoreEntities(persistedRecord, transaction);
            handleDeleteResolution(persistedRecord, transaction);
            persistenceEntityManager.deleteEntity(persistedRecord, transaction);
            return persistedRecord;
        }
        throw EntityRecordException.LK_NOTFOUND(entity, logicalKey);
    }

    private void handleDeleteResolution(EntityRecord entityRecord, Transaction transaction) throws GeminiException {
        ResolutionExecutor resolutionExecutor = ResolutionExecutor.forDelete(entityRecord, persistenceEntityManager, schemaManager, transaction);
        resolutionExecutor.run();
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
                    throw EntityException.ENTITY_FIELD_FOUND(fieldFromRecord);
                }
                schemaManager.deleteRuntimeEntityField(fieldFromRecord, transaction);
        }
    }

    private EntityRecord get(Entity entity, Collection<? extends Record.FieldValue> logicalKey, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> recordByLogicalKey = persistenceEntityManager.getRecordByLogicalKey(entity, logicalKey, transaction);
        if (recordByLogicalKey.isPresent()) {
            return recordByLogicalKey.get();
        }
        throw EntityRecordException.LK_NOTFOUND(entity, logicalKey);
    }

    private void checkEnabledState() throws InvalidStateException {
        State actualState = stateManager.getActualState();
        if (actualState.compareTo(State.SCHEMA_INITIALIZED) < 0) {
            throw InvalidStateException.STATE_LESS_THAN(actualState, State.SCHEMA_INITIALIZED);
        }
    }
}
