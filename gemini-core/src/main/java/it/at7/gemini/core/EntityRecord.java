package it.at7.gemini.core;

import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import it.at7.gemini.exceptions.InvalidTypeForObject;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

public class EntityRecord extends Record {
    private Entity entity;

    public EntityRecord(Entity entity) {
        Assert.notNull(entity, "Entity required for Entity Record");
        this.entity = entity;
    }

    public boolean set(String fieldName, Object value) {
        return put(fieldName, value);
    }

    public boolean put(String fieldName, Object value) {
        try {
            EntityField field = getEntityFieldFrom(fieldName);
            return put(field, value);
        } catch (EntityFieldException e) {
            return false;
        }
    }

    public boolean put(EntityField field, Object value) throws EntityFieldException {
        if (!(this.entity.getSchemaEntityFields().contains(field) || this.entity.getIdField().equals(field))) {
            throw EntityFieldException.ENTITYFIELD_NOT_FOUND(field);
        }
        return super.put(field, value);
    }

    public boolean put(Field field, Object value) {
        // Disabled - Put generic field on entity record.
        throw new UnsupportedOperationException("Not Available - use put(EntityField field, Object value)");
    }

    public Entity getEntity() {
        return entity;
    }

    public Set<EntityFieldValue> getLogicalKeyValue() {
        Set<EntityField> logicalKey = entity.getLogicalKey().getLogicalKeySet();
        return getEntityFieldValue(logicalKey);
    }

    /**
     * Get values and fields for all the fields available in the Entity Schema. This means
     * that if a new Entity Record is created with only a subset of fields the remaining fields
     * are extracted with a default value.
     *
     * @return
     */
    public Set<EntityFieldValue> getAllEntityFieldValues() {
        return getEntityFieldValue(entity.getSchemaEntityFields());
    }

    public Set<EntityFieldValue> getOnlyModifiedEntityFieldValue() {
        Set<EntityField> fields = (Set) getFields(); // cast is ok - invariant: put APIs ensures that
        return getEntityFieldValue(fields);
    }

    /**
     * Get a subset of Entity Fields
     *
     * @param fields filter fields
     * @return
     */
    public Set<EntityFieldValue> getEntityFieldValue(Set<EntityField> fields) {
        Set<EntityFieldValue> fieldValues = new HashSet<>();
        for (EntityField field : fields) {
            Object value = get(field);
            EntityFieldValue fieldValue = EntityFieldValue.create(field, value);
            fieldValues.add(fieldValue);
        }
        return fieldValues;
    }

    private EntityField getEntityFieldFrom(String fieldName) throws EntityFieldException {
        return this.entity.getField(fieldName);
    }

    public void update(EntityRecord rec) {
        Assert.isTrue(entity == rec.entity, "Records mus belong to the same Entity");
        for (EntityFieldValue fieldValue : rec.getOnlyModifiedEntityFieldValue()) {
            EntityField field = fieldValue.getEntityField();
            Object value = rec.get(field);
            try {
                put(field, value);
            } catch (EntityFieldException e) {
                // no exception here
                throw new RuntimeException("Critical bug here");
            }
        }
    }

    public boolean sameOf(EntityRecord entityRecord) {
        this.entity.equals(entityRecord.getEntity());
        return getAllEntityFieldValues().equals(entityRecord.getAllEntityFieldValues());
    }

    @Nullable
    public Object getID() {
        return get(getEntity().getIdField());
    }

    public FieldValue getIDFieldValueType() {
        return getFieldValue(getEntity().getIdField());
    }


    public static class EntityRecordsListWrapper {
        Collection<EntityRecord> records;

        public EntityRecordsListWrapper(Collection<EntityRecord> records) {
            this.records = records;
        }

        public Collection<EntityRecord> getRecords() {
            return records;
        }

        public static EntityRecordsListWrapper of(Collection<EntityRecord> records) {
            return new EntityRecordsListWrapper(records);
        }
    }


    public static class EntityFieldValue extends FieldValue {
        public EntityFieldValue(EntityField field, Object value) {
            super(field, value);
        }

        public EntityField getEntityField() {
            return (EntityField) getField();
        }

        public static EntityFieldValue create(EntityField field, Object value) {
            return new EntityFieldValue(field, value);
        }

        public static EntityFieldValue create(Entity entity, FieldValue value) throws EntityFieldException {
            EntityField field = entity.getField(value.getField().getName().toLowerCase());
            return new EntityFieldValue(field, value.getValue());
        }
    }

    public static class Converters extends Record.Converters {

        public static EntityRecord recordFromJSONMap(Entity entity, Map<String, Object> rawFields) throws InvalidLogicalKeyValue, InvalidTypeForObject {
            Map<String, Object> insensitiveFields = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            insensitiveFields.putAll(rawFields);
            EntityRecord entityRecord = new EntityRecord(entity);
            for (EntityField field : entity.getSchemaEntityFields()) {
                String key = field.getName().toLowerCase();
                Object objValue = insensitiveFields.get(key);
                if (objValue != null) {
                    try {
                        entityRecord.put(field, objValue);
                    } catch (EntityFieldException e) {
                        // this sould not happen because of the loop on the entityschemafields - chiamare la Madonna
                        throw new RuntimeException(String.format("record from JSON MAP critical bug: %s - %s", entity.getName(), field.getName()));
                    }
                }
            }
            Object idValue = rawFields.get(Field.ID_NAME);
            if (idValue != null) {
                try {
                    // this sould not happen because of the loop on the entityschemafields - chiamare la Madonna
                    entityRecord.put(entityRecord.getEntity().getIdField(), idValue);
                } catch (EntityFieldException e) {
                    throw new RuntimeException("record from JSON MAP critical bug");
                }
            }
            return entityRecord;
        }

        public static EntityRecord recordFromRawRecord(Entity entity, Record record) throws InvalidLogicalKeyValue {
            Map<String, Object> store = record.getStore();
            return recordFromJSONMap(entity, store);
        }

        public static Map<String, Object> toJSONMap(EntityRecord record) {
            Map<String, Object> convertedMap = new HashMap<>();
            for (FieldValue fieldValue : record.getAllEntityFieldValues()) {
                convertSingleFieldTOJSONValue(convertedMap, fieldValue);
            }
            return convertedMap;
        }

    }
}
