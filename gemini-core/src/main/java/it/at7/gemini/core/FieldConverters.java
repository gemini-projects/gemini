package it.at7.gemini.core;

import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static it.at7.gemini.core.FieldConverters.Formatter.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public class FieldConverters {

    public interface Formatter {
        DateTimeFormatter DATE_FORMATTER_INPUT = DateTimeFormatter.ofPattern("[yyyy-M-d][yyyy/M/d][d/M/yyyy]");
        DateTimeFormatter DATE_FORMATTER_OUTPUT = DateTimeFormatter.ofPattern("yyyy-M-d");
        DateTimeFormatter TIME_FORMATTER_INPUT = ISO_LOCAL_TIME;
        DateTimeFormatter TIME_FORMATTER_OUTPUT = ISO_LOCAL_TIME;
        DateTimeFormatter DATETIME_FORMATTER_INPUT = ISO_LOCAL_DATE_TIME;
        DateTimeFormatter DATETIME_FORMATTER_OUTPUT = ISO_LOCAL_DATE_TIME;
    }


    public static Object getConvertedFieldValue(Field field, Object objValue) {
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
                    pkValue = logicalKeyFromObject(field.getEntityRef(), objValue);
                }
                assert pkValue != null;
                return pkValue;
            case ENTITY_EMBEDED:
                assert objValue != null; // handled before
                if (EntityRecord.class.isAssignableFrom(objValue.getClass())) {
                    // no need to convert
                    return objValue;
                }
                Entity entityRef = field.getEntityRef();
                // need to check that the logicalKeyValue is right value
                if (Map.class.isAssignableFrom(objValue.getClass())) {
                    Map<String, Object> mapValue = (Map<String, Object>) objValue;
                    return RecordConverters.entityRecordFromMap(entityRef, mapValue);
                }
                break; // Unsupported OPE
            case TEXT_ARRAY:
                if (String[].class.isAssignableFrom(objValue.getClass())) {
                    return objValue;
                }
                if (Collection.class.isAssignableFrom(objValue.getClass())) {
                    String[] st = new String[((Collection) objValue).size()];
                    return ((Collection) objValue).toArray(st);
                }
                break; // Unsupported OPE
            case GENERIC_ENTITY_REF:
            case RECORD:
                break; // Unsupported OPE
        }
        throw new RuntimeException(String.format("Unsupported Operation: %s", field.toString()));
    }

    public static EntityReferenceRecord logicalKeyFromEntityRecord(EntityRecord entityRecord) {
        return logicalKeyFromObject(entityRecord.getEntity(), entityRecord);
    }

    @SuppressWarnings("unchecked")
    public static EntityReferenceRecord logicalKeyFromObject(Entity entity, Object value) {
        Entity.LogicalKey logicalKey = entity.getLogicalKey();
        List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
        if (logicalKeyList.isEmpty()) {
            return null; // NO action on empty logical key field
        }
        EntityReferenceRecord record = new EntityReferenceRecord(entity);
        if (logicalKeyList.size() == 1 && !Map.class.isAssignableFrom(value.getClass())) {
            // logicalKeyValue is the value
            Field field = logicalKeyList.get(0);
            if (RecordBase.class.isAssignableFrom(value.getClass())) {
                RecordBase rval = (RecordBase) value;
                value = rval.get(field);
            }
            record.addLogicalKeyValue(field, value);
        } else {
            if (RecordBase.class.isAssignableFrom(value.getClass())) {
                // we have a dynamic record
                value = ((RecordBase) value).getStore();
            }
            // need to check that the logicalKeyValue is right value
            if (!Map.class.isAssignableFrom(value.getClass())) {
                throw InvalidLogicalKeyValue.INVALID_VALUE_TYPE;
            }
            Map<String, Object> mapValue = (Map<String, Object>) value;
            EntityRecord refEntityRecord = RecordConverters.entityRecordFromMap(entity, mapValue);
            for (Field field : logicalKeyList) {
                String name = field.getName();
                Object fieldValue = refEntityRecord.get(name);
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
