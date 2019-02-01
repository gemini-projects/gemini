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

    public EntityRecord(Entity entity, Record record) {
        this.entity = entity;
        update(record);
    }

    public boolean put(String fieldName, Object value) {
        try {
            Field field = getEntityFieldFrom(fieldName);
            return put(field, value);
        } catch (EntityFieldException e) {
            return false;
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public Set<EntityFieldValue> getLogicalKeyValue() {
        Set<EntityField> logicalKey = entity.getLogicalKey().getLogicalKeySet();
        return getEntityFieldValues(logicalKey);
    }

    public Set<EntityFieldValue> getEntityFieldValues() {
        return getEntityFieldValues(entity.getSchemaEntityFields());
    }

    public Set<EntityFieldValue> getEntityFieldValues(Set<EntityField> fields) {
        Set<EntityFieldValue> fieldValues = new HashSet<>();
        for (EntityField field : fields) {
            Object value = get(field);
            EntityFieldValue fieldValue = EntityFieldValue.create(field, value);
            fieldValues.add(fieldValue);
        }
        return fieldValues;
    }


    private Field getEntityFieldFrom(String fieldName) throws EntityFieldException {
        return this.entity.getField(fieldName);
    }

    public void update(EntityRecord rec) {
        Assert.isTrue(entity == rec.entity, "Records mus belong to the same Entity");
        update((Record) rec);
    }

    public void update(Record rec) {
        for (FieldValue fieldValue : rec.getFieldValues()) {
            Field field = fieldValue.getField();
            Object value = rec.get(field);
            put(field, value);
        }
    }

    public boolean sameOf(EntityRecord entityRecord) {
        this.entity.equals(entityRecord.getEntity());
        return getEntityFieldValues().equals(entityRecord.getEntityFieldValues());
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
                    entityRecord.put(field, objValue);
                }
            }
            Object idValue = rawFields.get(Field.ID_NAME);
            if (idValue != null) {
                entityRecord.put(entityRecord.getEntity().getIdField(), idValue);
            }
            return entityRecord;
        }

        public static EntityRecord recordFromRawRecord(Entity entity, Record record) throws InvalidLogicalKeyValue {
            Map<String, Object> store = record.getStore();
            return recordFromJSONMap(entity, store);
        }

        public static Map<String, Object> toJSONMap(EntityRecord record) {
            Map<String, Object> convertedMap = new HashMap<>();
            for (FieldValue fieldValue : record.getEntityFieldValues()) {
                convertSingleFieldTOJSONValue(convertedMap, fieldValue);
            }
            return convertedMap;
        }

    }
}
