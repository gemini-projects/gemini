package it.at7.gemini;

import it.at7.gemini.core.*;
import it.at7.gemini.core.Module;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.core.persistence.PersistenceSchemaManager;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.util.*;

@Configuration
@EnableAutoConfiguration
public class UnitTestConfiguration {

    @Bean
    @Scope("prototype")
    public Transaction transaction() {
        return new Transaction() {
            @Override
            public void open() {

            }

            @Override
            public void close() throws GeminiException {

            }

            @Override
            public void commit() throws GeminiException {

            }

            @Override
            public void rollback() throws GeminiException {

            }
        };
    }

    @Bean
    public PersistenceSchemaManager persistenceSchemaManager() {
        return new PersistenceSchemaManager() {
            @Override
            public void beforeLoadSchema(Map<String, Module> modules, Transaction transaction) throws GeminiException, IOException {

            }

            @Override
            public void handleSchemaStorage(Transaction transaction, Collection<Entity> entities) throws GeminiException {

            }

            @Override
            public void deleteUnnecessaryEntites(Collection<Entity> entities, Transaction transaction) throws GeminiException {

            }

            @Override
            public void deleteUnnecessaryFields(Entity entity, Set<EntityField> fields, Transaction transaction) throws GeminiException {

            }

            @Override
            public void invokeCreateEntityStorageBefore(Entity entity, Transaction transaction) throws GeminiException {

            }

            @Override
            public boolean entityStorageExists(Entity entity, Transaction transaction) throws GeminiException {
                return true;
            }
        };
    }

    @Bean
    public PersistenceEntityManager persistenceEntityManager() {
        return new PersistenceEntityManager() {

            Map<String, Map<Key, EntityRecord>> store = new HashMap<>();
            Map<String, Long> ids = new HashMap<>();

            class Key {
                Map<String, Object> primitiveKey;

                public Key(Collection<? extends DynamicRecord.FieldValue> keyFieldValues) {
                    primitiveKey = RecordConverters.toMap(keyFieldValues);
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    Key key = (Key) o;
                    return Objects.equals(primitiveKey, key.primitiveKey);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(primitiveKey);
                }
            }

            @Override
            public List<EntityRecord> getEntityRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFieldValueType, EntityResolutionContext entityResolutionContext, Transaction transaction) throws GeminiException {
                String entityName = entity.getName().toUpperCase();
                Map<Key, EntityRecord> entityStorage = store.get(entityName);
                if (entityStorage == null) {
                    return new ArrayList<>();
                }
                List<EntityRecord> result = new ArrayList<>();
                for (EntityRecord value : entityStorage.values()) {
                    boolean equals = true;
                    for (DynamicRecord.FieldValue fieldValue : filterFieldValueType) {
                        Object storedValue = value.get(fieldValue.getField());
                        Object filterValue = fieldValue.getValue();

                        // TODO this code does not work
                        if (storedValue != null && filterValue != null && !storedValue.equals(filterValue)) {
                            equals = false;
                            break;
                        }
                    }
                    if (equals) {
                        result.add(value);
                    }
                }
                return result;
            }

            @Override
            public Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, Collection<? extends DynamicRecord.FieldValue> logicalKey, Transaction transaction) throws GeminiException {
                return getRecordByLogicalKeyInner(entity, logicalKey);
            }

            @Override
            public Optional<EntityRecord> getEntityRecordByLogicalKey(EntityRecord record, Transaction transaction) throws GeminiException {
                return getRecordByLogicalKeyInner(record.getEntity(), record.getLogicalKeyValue());
            }

            @Override
            public Optional<EntityRecord> getEntityRecordById(Entity entity, long recordId, Transaction transaction) throws GeminiException {
                return Optional.empty();
            }

            private Optional<EntityRecord> getRecordByLogicalKeyInner(Entity entity, Collection<? extends DynamicRecord.FieldValue> logicalKey) {
                String entityName = entity.getName().toUpperCase();
                Map<Key, EntityRecord> entityStorage = store.get(entityName);
                if (entityStorage == null) {
                    return Optional.empty();
                }
                Key key = new Key(logicalKey);
                EntityRecord entityRecord = entityStorage.get(key);
                if (entityRecord == null) return Optional.empty();
                for (EntityRecord.EntityFieldValue entityFieldValue : entityRecord.getAllSchemaEntityFieldValues()) {
                    EntityField entityField = entityFieldValue.getEntityField();
                    if (entityField.getType().equals(FieldType.ENTITY_REF)) {
                        Object value = entityFieldValue.getValue();
                        if (value instanceof Number) {
                            Entity entityRef = entityField.getEntityRef();
                            Map<Key, EntityRecord> keyEntityRecordMap = store.get(entityRef.getName().toUpperCase());
                            Optional<EntityRecord> first = keyEntityRecordMap.values().stream()
                                    .filter(r -> r.getIDEntityFieldValueType().getValue().equals(value))
                                    .findFirst();
                            EntityRecord refEntityRecord = first.get();
                            try {
                                entityRecord.put(entityField, refEntityRecord);
                            } catch (EntityFieldException e) {
                                // It should not happen because of the foreach on entity field values
                            }
                        }
                    }
                }
                return Optional.ofNullable(entityRecord);
            }


            @Override
            public EntityRecord createNewEntityRecord(EntityRecord record, Transaction transaction) throws GeminiException {
                Entity entity = record.getEntity();
                String entityName = entity.getName().toUpperCase();
                Map<Key, EntityRecord> entityStorage = store.computeIfAbsent(entityName, k -> new HashMap<>());
                Set<EntityRecord.EntityFieldValue> logicalKeyValue = record.getLogicalKeyValue();
                Key key = new Key(logicalKeyValue);
                EntityRecord existentRecord = entityStorage.get(key);
                if (existentRecord != null) {
                    // error ???
                }
                /* for (EntityRecord.EntityFieldValue entityFieldValue : record.getEntityFieldValue()) {
                    EntityField entityField = entityFieldValue.getEntityField();
                    if (entityField.getType().equals(FieldType.ENTITY_REF)) {
                        Object value = entityFieldValue.getValue();
                        if (value != null) {
                            Entity entityRef = entityField.getEntityRef();
                            EntityReferenceRecord entityReferenceRecord = (EntityReferenceRecord) value;
                            Optional<EntityRecord> recordByLogicalKey = getEntityRecordByLogicalKey(entityRef, entityReferenceRecord.getLogicalKeyRecord(), transaction);
                            EntityRecord entityRefRecord = recordByLogicalKey.get();
                            record.put(entityField.getName().toLowerCase(), entityRefRecord);
                        }
                    }
                } */
                Long lastId = ids.computeIfAbsent(entityName, k -> 0L) + 1;
                ids.put(entityName, lastId);
                record.put(entity.getIdEntityField(), lastId);
                entityStorage.put(key, record);
                return getEntityRecordByLogicalKey(record, transaction).get();
            }

            @Override
            public EntityRecord updateEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException {
                Entity entity = record.getEntity();
                String entityName = entity.getName().toUpperCase();
                Map<Key, EntityRecord> entityStorage = store.get(entityName);
                Optional<Map.Entry<Key, EntityRecord>> first = entityStorage.entrySet().stream()
                        .filter(r -> r.getValue().getIDEntityFieldValueType().getValue().equals(record.getIDEntityFieldValueType().getValue()))
                        .findFirst();
                assert first.isPresent();
                Map.Entry<Key, EntityRecord> entityRecord = first.get();
                Key key = entityRecord.getKey();
                EntityRecord value = entityRecord.getValue();
                Set<EntityRecord.EntityFieldValue> logicalKeyValue = value.getLogicalKeyValue();
                Key newKey = new Key(logicalKeyValue);
                entityStorage.remove(key);
                entityStorage.put(newKey, record);

                List<EntityField> entityReferenceFields = Services.getSchemaManager().getEntityReferenceFields(record.getEntity());
                for (EntityField entityReferenceField : entityReferenceFields) {
                    Entity refEntity = entityReferenceField.getEntity();
                    Map<Key, EntityRecord> entityRefStore = store.get(refEntity.getName().toUpperCase());
                    if (entityRefStore != null) {
                        for (EntityRecord refERec : entityRefStore.values()) {
                            EntityReferenceRecord eref = refERec.get(entityReferenceField);
                            if (eref != null) {
                                if (eref.getLogicalKeyRecord() != null &&
                                        eref.getLogicalKeyRecord().getFieldValues()
                                                .equals(RecordConverters.dynamicRecordFromMap(entity.getLogicalKey().getLogicalKeySet(), key.primitiveKey).getFieldValues())) {
                                    refERec.put(entityReferenceField, record);
                                }
                            }
                        }
                    }
                }
                return record;
            }

            @Override
            public void deleteEntityRecordByID(EntityRecord record, Transaction transaction) {
                Entity entity = record.getEntity();
                String entityName = entity.getName().toUpperCase();
                Map<Key, EntityRecord> entityStorage = store.get(entityName);
                Set<EntityRecord.EntityFieldValue> logicalKeyValue = record.getLogicalKeyValue();
                Key key = new Key(logicalKeyValue);
                assert entityStorage.containsKey(key);
                entityStorage.remove(key);
            }

            @Override
            public EntityRecord createOrUpdateEntityRecord(EntityRecord entityRecord, Transaction transaction) throws GeminiException {
                Optional<EntityRecord> recordByLogicalKey = getEntityRecordByLogicalKey(entityRecord, transaction);
                if (recordByLogicalKey.isPresent()) {
                    EntityRecord persistedEntityRecord = recordByLogicalKey.get();
                     /*EntityField idField = entityRecord.getEntity().getIdEntityField();
                    Object persistedID = persistedEntityRecord.get(idField);
                    entityRecord.put(idField, persistedID);
                    return updateEntityRecordByID(entityRecord, transaction); */

                } else {
                    return createNewEntityRecord(entityRecord, transaction);
                }
                return entityRecord;
            }

           /* @Override
            public int updateEntityRecordsMatchingFilter(Entity entity, Collection<EntityRecord.EntityFieldValue> filterFieldValueType, Collection<EntityRecord.EntityFieldValue> updateWith, Transaction transaction) throws GeminiException {
                int updated = 0;
                Map<Key, EntityRecord> entityStorage = store.get(entity.getName().toUpperCase());
                if (entityStorage != null) {
                    for (EntityRecord er : entityStorage.values()) {
                        boolean equals = true;
                        for (EntityRecord.EntityFieldValue entityFieldValue : filterFieldValueType) {
                            EntityField entityField = entityFieldValue.getEntityField();
                            Object entityValue = er.get(entityField);
                            Object filterValue = entityFieldValue.getValue();
                            if (!filterValue.equals(entityValue)) {
                                equals = false;
                                continue;
                            }
                        }
                        if (!equals) {
                            for (EntityRecord.EntityFieldValue updateValue : updateWith) {
                                EntityField entityField = updateValue.getEntityField();
                                Object value = updateValue.getValue();
                                if (entityField.getType().equals(FieldType.ENTITY_REF)) {
                                    EntityReferenceRecord rR = (EntityReferenceRecord) value;
                                    if (rR.hasPrimaryKey() && rR.getPrimaryKey().equals(0L)) {
                                        value = null;
                                    }
                                }
                                er.put(updateValue.getEntityField(), value);
                            }
                        }
                    }
                }
                return updated;
            } */

            @Override
            public List<EntityRecord> getEntityRecordsMatching(Entity entity, FilterContext filterContext, EntityResolutionContext entityResolutionContext, Transaction transaction) throws GeminiException {
                String entityName = entity.getName().toUpperCase();
                Map<Key, EntityRecord> entityStorage = store.get(entityName);
                if (entityStorage == null) {
                    return null;
                }
                return new ArrayList<>(entityStorage.values());
            }
        };
    }

}
