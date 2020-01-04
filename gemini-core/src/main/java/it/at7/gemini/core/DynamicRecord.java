package it.at7.gemini.core;

import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;

import java.util.*;

public class DynamicRecord implements RecordBase {
    private Map<String, Object> store;
    private Set<Field> fields;

    public DynamicRecord() {
        this.store = new HashMap<>();
        this.fields = new HashSet<>();
    }

    public Set<Field> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    @Override
    public Map<String, Object> getStore() {
        return store;
    }

    public boolean set(String fieldName, Object value) {
        return put(fieldName, value);
    }

    public boolean put(String fieldName, Object value) {
        Field field = getFieldFrom(fieldName, value);
        return put(field, value);
    }

    public boolean put(Field field, Object value) {
        Object convertedValue = FieldConverters.getConvertedFieldValue(field, value);
        fields.add(field);
        store.put(field.getName().toLowerCase(), convertedValue);
        return true;
    }

    private Field getFieldFrom(String fieldName, Object value) {
        if (String.class.isAssignableFrom(value.getClass())) {
            return new Field(FieldType.TEXT, fieldName);
        }
        if (Number.class.isAssignableFrom(value.getClass())) {
            return new Field(FieldType.NUMBER, fieldName);
        }
        if (Boolean.class.isAssignableFrom(value.getClass())) {
            return new Field(FieldType.BOOL, fieldName);
        }
        if (EntityRecord.class.isAssignableFrom(value.getClass())) {
            EntityRecord entityValue = (EntityRecord) value;
            return new Field(FieldType.ENTITY_REF, fieldName, entityValue.getEntity().getName());
        }
        if (DynamicRecord.class.isAssignableFrom(value.getClass())) {
            return new Field(FieldType.RECORD, fieldName);
        }
        throw new RuntimeException("Unsupported Operation");
    }

    public FieldValue getFieldValue(Field field) {
        Object value = get(field);
        return FieldValue.create(field, value);
    }

    public Set<FieldValue> getFieldValues(Set<? extends Field> fields) {
        Set<FieldValue> fieldValues = new HashSet<>();
        for (Field field : fields) {
            Object value = get(field);
            FieldValue fieldValue = FieldValue.create(field, value);
            fieldValues.add(fieldValue);
        }
        return fieldValues;
    }

    public Set<FieldValue> getFieldValues() {
        return getFieldValues(fields);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicRecord that = (DynamicRecord) o;
        return Objects.equals(store, that.store) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store, fields);
    }

    @Override
    public String toString() {
        return "DynamicRecord{" +
                "store=" + store +
                '}';
    }
}
