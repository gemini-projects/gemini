package it.at7.gemini.schema;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static it.at7.gemini.schema.Field.Converters.Formatter.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public class Field {
    public static final String ID_NAME = "id";
    // public static final Field ID = new Field(FieldType.PK, "id", true);

    private final FieldType fieldType;
    private final String fieldName;
    private final String entityRefName;
    private final String entityCollectionRefField;


    public Field(Field field) {
        this.fieldType = field.fieldType;
        this.fieldName = field.fieldName;
        this.entityRefName = field.entityRefName;
        this.entityCollectionRefField = field.entityCollectionRefField;
    }

    public Field(FieldType fieldType, String fieldName) {
        this(fieldType, fieldName, null, null);
    }

    public Field(FieldType fieldType, String fieldName, String entityRefName) {
        this(fieldType, fieldName, entityRefName, null);
    }

    public Field(FieldType fieldType, String fieldName, String entityRefName, String entityCollectionRefField) {
        Assert.notNull(fieldType, "FielType required for Field");
        Assert.notNull(fieldName, "Name required for Field");
        if (fieldType == FieldType.ENTITY_REF) {
            Assert.notNull(entityRefName, String.format("Entity Name Required for %s FieldType", FieldType.ENTITY_REF.name()));
        }
        if (fieldType == FieldType.ENTITY_COLLECTION_REF) {
            Assert.notNull(entityCollectionRefField, String.format("Entity Collection Field Name Required for %s FieldType", FieldType.ENTITY_COLLECTION_REF.name()));
        }
        this.entityRefName = entityRefName;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.entityCollectionRefField = entityCollectionRefField;
    }


    public FieldType getType() {
        return fieldType;
    }

    public String getName() {
        return fieldName;
    }

    /**
     * @return Entity reference if the fieldtype is a reference
     */
    @Nullable
    public Entity getEntityRef() {
        if (entityRefName != null && !entityRefName.isEmpty()) {
            SchemaManager schemaManager = Services.getSchemaManager();
            assert schemaManager != null;
            return schemaManager.getEntity(entityRefName);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return fieldType == field.fieldType &&
                Objects.equals(fieldName, field.fieldName) &&
                Objects.equals(entityRefName, field.entityRefName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, fieldName, entityRefName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Field{");
        sb.append("fieldType=").append(fieldType);
        sb.append(", fieldName='").append(fieldName).append('\'');
        sb.append(", entityRefName='").append(entityRefName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public interface Converters {

        interface Formatter {
            DateTimeFormatter DATE_FORMATTER_INPUT = DateTimeFormatter.ofPattern("[yyyy-M-d][yyyy/M/d][d/M/yyyy]");
            DateTimeFormatter DATE_FORMATTER_OUTPUT = DateTimeFormatter.ofPattern("yyyy-M-d");
            DateTimeFormatter TIME_FORMATTER_INPUT = ISO_LOCAL_TIME;
            DateTimeFormatter TIME_FORMATTER_OUTPUT = ISO_LOCAL_TIME;
            DateTimeFormatter DATETIME_FORMATTER_INPUT = ISO_LOCAL_DATE_TIME;
            DateTimeFormatter DATETIME_FORMATTER_OUTPUT = ISO_LOCAL_DATE_TIME;
        }


        static Object getConvertedFieldValue(Field field, Object objValue) {
            if (objValue == null) {
                return null;
            }
            FieldType type = field.getType();
            String stValue = String.valueOf(objValue);
            switch (type) {
                case PK:
                    return objValue;
                case TEXT:
                case TRANSL_TEXT:
                    return stValue;
                case NUMBER:
                    try {
                        return Long.parseLong(stValue);
                    } catch (NumberFormatException e) {
                        try {
                            return Double.parseDouble(stValue);
                        } catch (NumberFormatException e1) {
                            // ignore
                            return null;
                        }
                    }
                case LONG:
                    if (objValue instanceof Long) {
                        return objValue;
                    }
                    return Long.parseLong(stValue);
                case DOUBLE:
                    if (objValue instanceof Double) {
                        return objValue;
                    }
                    return Double.parseDouble(stValue);
                case BOOL:
                    if (objValue instanceof Boolean) {
                        return objValue;
                    } else {
                        return Boolean.parseBoolean(stValue);
                    }
                case TIME:
                    if (LocalTime.class.isAssignableFrom(objValue.getClass())) {
                        return objValue;
                    }
                    return LocalTime.parse(stValue, TIME_FORMATTER_INPUT);
                case DATE:
                    if (LocalDate.class.isAssignableFrom(objValue.getClass())) {
                        return objValue;
                    }
                    return LocalDate.parse(stValue, DATE_FORMATTER_INPUT);
                case DATETIME:
                    if (LocalDateTime.class.isAssignableFrom(objValue.getClass())) {
                        return objValue;
                    }
                    return LocalDateTime.parse(stValue, DATETIME_FORMATTER_INPUT);
                case ENTITY_REF:
                    assert objValue != null; // handled before
                    EntityField entityField = EntityField.class.cast(field);
                    if (EntityReferenceRecord.class.isAssignableFrom(objValue.getClass())) {
                        // no need to convert
                        return objValue;
                    }
                    EntityReferenceRecord pkValue = null;
                    if (EntityRecord.class.isAssignableFrom(objValue.getClass())) {
                        EntityRecord entityRecord = (EntityRecord) objValue;
                        Entity fieldEntity = field.getEntityRef();
                        Entity objValueEntity = entityRecord.getEntity();
                        assert fieldEntity.equals(objValueEntity);
                        pkValue = logicalKeyFromEntityRecord(entityRecord);
                    } else {
                        pkValue = logicalKeyFromObject(entityField.getEntityRef(), objValue);
                    }
                    assert pkValue != null;
                    return pkValue;
                case TEXT_ARRAY:
                    if (String[].class.isAssignableFrom(objValue.getClass())) {
                        return objValue;
                    }
                    if (Collection.class.isAssignableFrom(objValue.getClass())) {
                        String[] st = new String[((Collection) objValue).size()];
                        return ((Collection) objValue).toArray(st);
                    }
                    throw new RuntimeException("Unsupported Operation");
                case GENERIC_ENTITY_REF:
                case RECORD:
                    throw new RuntimeException("Unsupported Operation");
            }
            throw new RuntimeException(String.format("Unsupported Operation: %s", field.toString()));
        }

        static EntityReferenceRecord logicalKeyFromEntityRecord(EntityRecord entityRecord) {
            return logicalKeyFromObject(entityRecord.getEntity(), entityRecord);
        }

        /* static Record.FieldValue logicalKeyFromStrings(Entity entity, String...keys) {
            Entity.LogicalKey logicalKey = entity.getLogicalKey();
            List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
            assert keys.length == logicalKeyList.size();
            List<Record.FieldValue> lkFieldValues = new ArrayList<>();
            for(int i= 0;i<logicalKeyList.size();i++){
                EntityField field = logicalKeyList.get(i);
                String lkElem = keys[i];
                lkFieldValues.add(EntityRecord.EntityFieldValue.create(field, lkElem));
            }
            ArrayList<Field> fields = new ArrayList<>(logicalKeySet);
            Field field = fields.get(0);
            Object convertedFieldValue = getConvertedFieldValue(field, key);
            return Record.FieldValue.create(field, convertedFieldValue);
        } */

        @SuppressWarnings("unchecked")
        static EntityReferenceRecord logicalKeyFromObject(Entity entity, Object value) {
            Entity.LogicalKey logicalKey = entity.getLogicalKey();
            List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
            if (logicalKeyList.isEmpty()) {
                return null; // NO action on empty logical key field
            }
            EntityReferenceRecord record = new EntityReferenceRecord(entity);
            if (logicalKeyList.size() == 1) {
                // logicalKeyValue is the value
                Field field = logicalKeyList.get(0);
                if (Record.class.isAssignableFrom(value.getClass())) {
                    Record rval = (Record) value;
                    value = rval.get(field);
                }
                record.addLogicalKeyValue(field, value);
            } else {
                if (Record.class.isAssignableFrom(value.getClass())) {
                    // we have a dynamic record
                    value = ((Record) value).getStore();
                }
                // need to check that the logicalKeyValue is right value
                if (!Map.class.isAssignableFrom(value.getClass())) {
                    throw InvalidLogicalKeyValue.INVALID_VALUE_TYPE;
                }
                Map<String, Object> mapValue = (Map<String, Object>) value;
                for (Field field : logicalKeyList) {
                    String name = field.getName();
                    Object fieldValue = mapValue.get(name);
                    if (fieldValue == null) {
                        throw InvalidLogicalKeyValue.KEY_FIELD_NOTEXISTS(name);
                    }
                    Object convertedFieldValue = getConvertedFieldValue(field, fieldValue);
                    record.addLogicalKeyValue(field, convertedFieldValue);
                }
            }
            return record;
        }

    }

}
