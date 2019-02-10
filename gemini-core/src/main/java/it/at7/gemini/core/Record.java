package it.at7.gemini.core;

import it.at7.gemini.exceptions.FieldException;
import it.at7.gemini.exceptions.InvalidTypeForObject;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static it.at7.gemini.schema.Field.Converters.Formatter.DATETIME_FORMATTER_OUTPUT;
import static it.at7.gemini.schema.Field.Converters.Formatter.DATE_FORMATTER_OUTPUT;
import static it.at7.gemini.schema.Field.Converters.Formatter.TIME_FORMATTER_OUTPUT;
import static it.at7.gemini.schema.Field.Converters.getConvertedFieldValue;

public class Record {

    private Map<String, Object> store;
    private Set<Field> fields;

    public Record() {
        store = new HashMap<>();
        fields = new HashSet<>();
    }

    /* private void put(Field field, Object value) {
        fields.add(field);
        store.put(field.getName(), value);
    }*/

    public boolean set(String fieldName, Object value) {
        return put(fieldName, value);
    }

    public boolean put(String fieldName, Object value) {
        Field field = getFieldFrom(fieldName, value);
        assert field != null;
        return put(field, value);
    }

    public boolean put(Field field, Object value) {
        Object convertedValue = getConvertedFieldValue(field, value);
        fields.add(field);
        store.put(field.getName().toLowerCase(), convertedValue);
        return true;
    }

    public Set<Field> getFields() {
        return Collections.unmodifiableSet(fields);
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
        if (Record.class.isAssignableFrom(value.getClass())) {
            return new Field(FieldType.RECORD, fieldName);
        }
        throw new RuntimeException("Unsupported Operation");
    }

    public Map<String, Object> getStore() {
        return store;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(String field) throws InvalidTypeForObject {
        Object o = store.get(field.toLowerCase());
        if (o == null) {
            return null;
        }
        try {
            return (T) o;
        } catch (ClassCastException e) {
            throw new InvalidTypeForObject();
        }
    }

    public <T> T getRequiredField(String field) throws InvalidTypeForObject, FieldException {
        T value = get(field);
        if (value == null) {
            throw FieldException.FIELD_NOT_FOUND(field);
        }
        return value;
    }

    public <T> T getFieldOrDefault(String field, T def) {
        return getFieldOrDefault(field, def, false);
    }


    public <T> T getFieldOrSetDefault(String field, T def) {
        return getFieldOrDefault(field, def, true);

    }

    private <T> T getFieldOrDefault(String field, T def, boolean set) {
        T value = get(field);
        if (set && value == null) {
            put(field, def);
        }
        return value == null ? def : value;
    }

    @Nullable
    public <T> T get(Field field, Class<T> fieldType) {
        return get(field.getName(), fieldType);
    }

    @Nullable
    public <T> T get(String field, Class<T> fieldType) {
        try {
            Object value = get(field);
            return value == null ? null : fieldType.cast(value);
        } catch (InvalidTypeForObject invalidTypeForObject) {
            // mai qua
            return null;
        }
    }

    @Nullable
    public <T> T get(Field field) {
        return get(field.getName());
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

    public static class Converters {
        public static Record recordFromMap(Collection<? extends Field> fields, Map<String, Object> rawFields) throws InvalidTypeForObject {
            Record record = new Record();
            for (Field field : fields) {
                String key = field.getName().toLowerCase();
                Object objValue = rawFields.get(key);
                if (objValue != null) {
                    putValueToRecord(record, field, objValue);
                }
            }
            return record;
        }

        public static Map<String, Object> toMap(Record record) {
            return toMap(record.getFieldValues());
        }

        public static Map<String, Object> toMap(Collection<? extends FieldValue> fieldValues) {
            Map<String, Object> convertedMap = new HashMap<>();
            for (FieldValue fieldValue : fieldValues) {
                convertSingleFieldTOJSONValue(convertedMap, fieldValue);
            }
            return convertedMap;
        }

        static void putValueToRecord(Record r, Field field, @Nullable Object objValue) {
            if (objValue == null) {
                r.put(field, null);
                return;
            }
            r.put(field, objValue);
        }

        static protected void convertSingleFieldTOJSONValue(Map<String, Object> convertedMap, FieldValue fieldValue) {
            Field field = fieldValue.getField();
            FieldType fieldType = field.getType();
            String fieldNameLC = field.getName();
            Object value = fieldValue.getValue();
            if (value == null) {
                value = nullToDefault(field);
            }
            switch (fieldType) {
                case PK:
                case LONG:
                case DOUBLE:
                case TEXT:
                case TRANSL_TEXT:
                case NUMBER:
                case BOOL:
                case RECORD:
                case TEXT_ARRAY:
                    convertedMap.put(fieldNameLC, value);
                    break;
                case TIME:
                    if (String.class.isAssignableFrom(value.getClass())) {
                        String stValue = (String) value;
                        if (!stValue.isEmpty()) {
                            // TODO handle check the right format or try to convert
                        }
                        convertedMap.put(fieldNameLC, value);
                    }
                    if (LocalTime.class.isAssignableFrom(value.getClass())) {
                        // TODO
                        LocalTime ltValue = (LocalTime) value;
                        convertedMap.put(fieldNameLC, ltValue.format(TIME_FORMATTER_OUTPUT));
                    }
                    break;
                case DATE:
                    if (String.class.isAssignableFrom(value.getClass())) {
                        String stValue = (String) value;
                        if (!stValue.isEmpty()) {
                            // TODO handle check the right format or try to convert
                        }
                        convertedMap.put(fieldNameLC, value);
                    }
                    if (LocalDate.class.isAssignableFrom(value.getClass())) {
                        LocalDate ldValue = (LocalDate) value;
                        convertedMap.put(fieldNameLC, ldValue.format(DATE_FORMATTER_OUTPUT));
                    }
                    break;
                case DATETIME:
                    if (String.class.isAssignableFrom(value.getClass())) {
                        String stValue = (String) value;
                        if (!stValue.isEmpty()) {
                            // TODO handle check the right format or try to convert
                        }
                        convertedMap.put(fieldNameLC, value);
                    }
                    if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
                        // TODO
                        LocalDateTime ltValue = (LocalDateTime) value;
                        convertedMap.put(fieldNameLC, ltValue.format(DATETIME_FORMATTER_OUTPUT));
                    }
                    break;
                case ENTITY_REF:
                    convertEntityRefToJSONValue(convertedMap, field, value);
                    break;
                default:
                    throw new RuntimeException(String.format("No conversion found for fieldtype %s", fieldType));
            }
        }

        private static void convertEntityRefToJSONValue(Map<String, Object> convertedMap, Field field, Object value) {
            String fieldNameLC = field.getName();
            if (EntityReferenceRecord.class.isAssignableFrom(value.getClass())) {
                EntityReferenceRecord pkRefRec = (EntityReferenceRecord) value;
                if (pkRefRec.equals(EntityReferenceRecord.NO_REFERENCE)) {
                    convertedMap.put(fieldNameLC, new HashMap<>()); // empty map -> empty json
                } else {
                    Entity entity = pkRefRec.getEntity();
                    assert field.getEntityRef().equals(entity);
                    convertedMap.put(fieldNameLC, toLogicalKey(pkRefRec));
                }
            } else if (EntityRecord.class.isAssignableFrom(value.getClass())) {
                // we have the full reference record here -- we add a map of its fields
                EntityRecord eRValue = (EntityRecord) value;
                convertedMap.put(fieldNameLC, toMap(eRValue));
            }

        }

        private static Object toLogicalKey(EntityReferenceRecord pkRefRec) {
            if (pkRefRec.hasPrimaryKey() && pkRefRec.getPrimaryKey().equals(0L)) {
                return null; // null value if we have a no key;
            }
            assert pkRefRec.hasLogicalKey();
            Record lkValue = pkRefRec.getLogicalKeyRecord();
            Entity.LogicalKey lk = pkRefRec.getEntity().getLogicalKey();
            List<EntityField> lkFields = lk.getLogicalKeyList();
            if (lkFields.size() == 1) {
                Object lkSingleValue = lkValue.get(lkFields.get(0));
                assert lkSingleValue != null;
                return lkSingleValue;
            }
            Map<String, Object> convertedMap = new HashMap<>();
            Entity entity = pkRefRec.getEntity();
            Record logicalKeyValue = pkRefRec.getLogicalKeyRecord();
            for (EntityField entityField : entity.getLogicalKey().getLogicalKeyList()) {
                FieldValue fieldValue = logicalKeyValue.getFieldValue(entityField);
                convertSingleFieldTOJSONValue(convertedMap, fieldValue);
            }
            return convertedMap;
        }

        public static List<EntityRecord.EntityFieldValue> logicalKeyFromStrings(Entity entity, String... keys) {
            Entity.LogicalKey logicalKey = entity.getLogicalKey();
            List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
            assert keys.length == logicalKeyList.size();
            List<EntityRecord.EntityFieldValue> lkFieldValues = new ArrayList<>();
            for (int i = 0; i < logicalKeyList.size(); i++) {
                EntityField field = logicalKeyList.get(i);
                String lkElem = keys[i];
                Object convertedLkElem = Field.Converters.getConvertedFieldValue(field, lkElem);
                lkFieldValues.add(EntityRecord.EntityFieldValue.create(field, convertedLkElem));
            }
            return lkFieldValues;
        }


        static Object nullToDefault(Field field) {
            FieldType fieldType = field.getType();
            switch (fieldType) {
                case PK:
                    return 0;
                case TEXT:
                case TIME:
                case DATE:
                case DATETIME:
                case TRANSL_TEXT:
                    return "";
                case NUMBER:
                    return 0;
                case LONG:
                    return 0;
                case DOUBLE:
                    return 0.;
                case BOOL:
                    return false;
                case ENTITY_REF:
                    return EntityReferenceRecord.NO_REFERENCE;
                case RECORD:
                    return new Object();
                case TEXT_ARRAY:
                    return new String[]{};
            }
            throw new RuntimeException(String.format("No default found for type %s", field));
        }

    }

    /**
     * A simple container to hold a Field and its Value
     */
    public static class FieldValue {
        private final Field field;
        private final Object value;

        FieldValue(Field field, @Nullable Object value) {
            this.field = field;
            this.value = value;
        }

        public Field getField() {
            return field;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldValue that = (FieldValue) o;
            return Objects.equals(field, that.field) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, value);
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("FieldValue{");
            sb.append("field=").append(field);
            sb.append(", value=").append(value);
            sb.append('}');
            return sb.toString();
        }

        public static FieldValue create(Field field, Object value) {
            return new FieldValue(field, value);
        }
    }
}
