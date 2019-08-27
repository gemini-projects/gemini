package it.at7.gemini.core;

import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import it.at7.gemini.exceptions.InvalidTypeForObject;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;
import org.springframework.lang.Nullable;

import java.time.*;
import java.util.*;

import static it.at7.gemini.core.utils.DateTimeUtility.Formatter.*;


public class RecordConverters {
    public static final String GEMINI_DATA_FIELD = "data";
    public static final String GEMINI_META_FIELD = "meta";
    public static final String GEMINI_UUID_FIELD = "uuid";
    public static final String GEMINI_META_ENTITY_FIELD = "entity";

    // TODO handle dates

    public static boolean containGeminiDataTypeFields(Map<String, Object> rawFields) {
        return rawFields.containsKey(GEMINI_DATA_FIELD) && rawFields.containsKey(GEMINI_META_FIELD);
    }

    public static EntityRecord entityRecordFromMap(Entity entity, Map<String, Object> fieldMap) throws InvalidLogicalKeyValue, InvalidTypeForObject {
        Map<String, Object> rawFields = fieldMap;
        if (containGeminiDataTypeFields(rawFields)) {
            Object rawFieldsOBJ = rawFields.get(GEMINI_DATA_FIELD);
            if (Map.class.isAssignableFrom(rawFieldsOBJ.getClass())) {
                rawFields = (Map<String, Object>) rawFieldsOBJ;
            }
        }
        Map<String, Object> insensitiveFields = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        insensitiveFields.putAll(rawFields);
        EntityRecord entityRecord = new EntityRecord(entity);
        for (EntityField field : entity.getDataEntityFields()) {
            String key = toFieldName(field).toLowerCase();
            Object objValue = insensitiveFields.get(key);

            if (insensitiveFields.containsKey(key)) {
                try {
                    entityRecord.put(field, objValue);
                } catch (EntityFieldException e) {
                    // this sould not happen because of the loop on the entityschemafields - chiamare la Madonna
                    throw new RuntimeException(String.format("record from JSON MAP critical bug: %s - %s", entity.getName(), toFieldName(field)));
                }
            }
        }
        Object idValue = rawFields.get(Field.ID_NAME);
        if (idValue != null) {
            try {
                // this sould not happen because of the loop on the entityschemafields - chiamare la Madonna
                entityRecord.put(entityRecord.getEntity().getIdEntityField(), idValue);
            } catch (EntityFieldException e) {
                throw new RuntimeException("record from JSON MAP critical bug");
            }
        }
        return entityRecord;
    }

    public static DynamicRecord dynamicRecordFromMap(Collection<? extends Field> fields, Map<String, Object> rawFields) throws InvalidTypeForObject {
        DynamicRecord record = new DynamicRecord();
        for (Field field : fields) {
            String key = toFieldName(field);
            Object objValue = rawFields.get(key);
            if (objValue != null) {
                putValueToRecord(record, field, objValue);
            }
        }
        return record;
    }

    public static EntityRecord entityRecordFromDynamicRecord(Entity entity, DynamicRecord record) throws InvalidLogicalKeyValue {
        Map<String, Object> store = record.getStore();
        return entityRecordFromMap(entity, store);
    }

    public static Map<String, Object> fieldsToJSONMap(EntityRecord record) {
        Map<String, Object> convertedMap = new HashMap<>();
        for (EntityFieldValue fieldValue : record.getDataEntityFieldValues()) {
            convertSingleFieldTOJSONValue(convertedMap, fieldValue);
        }
        return convertedMap;
    }

    public static Map<String, Object> metaToJSONMap(EntityRecord record) {
        Map<String, Object> convertedMap = new HashMap<>();
        convertedMap.put(GEMINI_META_ENTITY_FIELD, record.getEntity().getName());
        if (!record.getEntity().isEmbedable()) {
            convertedMap.put(GEMINI_UUID_FIELD, record.getUUID());
        }
        for (EntityFieldValue fieldValue : record.getMetaEntityFieldValues()) {
            convertSingleFieldTOJSONValue(convertedMap, fieldValue);
        }
        return convertedMap;
    }


    public static Map<String, Object> toMap(EntityRecord record) {
        return toMap(record.getDataEntityFieldValues());
    }

    public static Map<String, Object> toMap(DynamicRecord record) {
        return toMap(record.getFieldValues());
    }

    public static Map<String, Object> toMap(Collection<? extends FieldValue> fieldValues) {
        Map<String, Object> convertedMap = new HashMap<>();
        for (FieldValue fieldValue : fieldValues) {
            convertSingleFieldTOJSONValue(convertedMap, fieldValue);
        }
        return convertedMap;
    }

    static void putValueToRecord(DynamicRecord r, Field field, @Nullable Object objValue) {
        if (objValue == null) {
            r.put(field, null);
            return;
        }
        r.put(field, objValue);
    }

    static protected void convertSingleFieldTOJSONValue(Map<String, Object> convertedMap, FieldValue fieldValue) {
        Field field = fieldValue.getField();
        FieldType fieldType = field.getType();
        String fieldNameLC = toFieldName(field);
        Object value = fieldValue.getValue();
        if (value == null) {
            value = nullToDefault(field);
        }
        switch (fieldType) {
            case PK:
            case LONG:
            case DOUBLE:
            case TEXT:
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
                    LocalTime ltValue = (LocalTime) value;
                    OffsetTime utcTime = OffsetTime.of(ltValue, ZoneOffset.UTC);
                    convertedMap.put(fieldNameLC, utcTime.format(TIME_FORMATTER_OUTPUT));
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
                    LocalDateTime ltValue = (LocalDateTime) value;
                    OffsetDateTime utcDateTime = OffsetDateTime.of(ltValue, ZoneOffset.UTC);
                    convertedMap.put(fieldNameLC, utcDateTime.format(DATETIME_FORMATTER_OUTPUT));
                }
                break;
            case ENTITY_REF:
                convertEntityRefToJSONValue(convertedMap, field, value);
                break;
            case ENTITY_EMBEDED:
                convertEntityEmbededTOJsonValue(convertedMap, field, value);
                break;
            case ENTITY_REF_ARRAY:
                convertEntityRefArrayToJSONValue(convertedMap, field, value);
                break;
            case PASSWORD:
                break; // ignore password in API
            default:
                throw new RuntimeException(String.format("No conversion found for fieldtype %s", fieldType));
        }
    }

    private static void convertEntityRefArrayToJSONValue(Map<String, Object> convertedMap, Field field, Object value) {
        List<Object> refArray = new ArrayList<>();
        if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection genericColl = (Collection) value;
            if (genericColl.iterator().hasNext()) {
                Object firstVal = genericColl.iterator().next();
                if (EntityReferenceRecord.class.isAssignableFrom(firstVal.getClass())) {
                    Collection<EntityReferenceRecord> entityReferenceRecords = (Collection<EntityReferenceRecord>) value;
                    for (EntityReferenceRecord entityReferenceRecord : entityReferenceRecords) {
                        refArray.add(toLogicalKey(entityReferenceRecord));
                    }
                } else {
                    // TODO
                    throw new RuntimeException("TODO convertEntityRefArrayToJSONValue for EntityRecord array");
                }
            }
        }
        convertedMap.put(toFieldName(field), refArray);
    }

    private static void convertEntityRefToJSONValue(Map<String, Object> convertedMap, Field field, Object value) {
        String fieldNameLC = toFieldName(field);
        if (value == null) {
            convertedMap.put(fieldNameLC, new HashMap<>());
            return;
        }
        if (EntityReferenceRecord.class.isAssignableFrom(value.getClass())) {
            EntityReferenceRecord pkRefRec = (EntityReferenceRecord) value;
            convertedMap.put(fieldNameLC, toLogicalKey(pkRefRec));
        } else if (EntityRecord.class.isAssignableFrom(value.getClass())) {
            // we have the full reference record here -- we add a map of its fields
            EntityRecord eRValue = (EntityRecord) value;
            convertedMap.put(fieldNameLC, toMap(eRValue));
        }
    }

    private static void convertEntityEmbededTOJsonValue(Map<String, Object> convertedMap, Field field, Object value) {
        String fieldName = field.getName();
        if (value == null) {
            convertedMap.put(fieldName, new HashMap<>());
            return;
        }
        if (EntityRecord.class.isAssignableFrom(value.getClass())) {
            EntityRecord eRValue = (EntityRecord) value;
            convertedMap.put(fieldName, toMap(eRValue));
            return;
        }
        throw new RuntimeException("Unsupported OPE");
    }

    private static Object toLogicalKey(EntityReferenceRecord pkRefRec) {
        if (pkRefRec.hasPrimaryKey() && pkRefRec.getPrimaryKey().equals(0L)) {
            return null; // null value if we have a no key;
        }
        assert pkRefRec.hasLogicalKey();
        DynamicRecord lkValue = pkRefRec.getLogicalKeyRecord();
        Entity.LogicalKey lk = pkRefRec.getEntity().getLogicalKey();
        List<EntityField> lkFields = lk.getLogicalKeyList();
        if (lkFields.size() == 1) {
            Object lkSingleValue = lkValue.get(lkFields.get(0));
            assert lkSingleValue != null;
            return lkSingleValue;
        }
        Map<String, Object> convertedMap = new HashMap<>();
        Entity entity = pkRefRec.getEntity();
        DynamicRecord logicalKeyValue = pkRefRec.getLogicalKeyRecord();
        for (EntityField entityField : entity.getLogicalKey().getLogicalKeyList()) {
            FieldValue fieldValue = logicalKeyValue.getFieldValue(entityField);
            convertSingleFieldTOJSONValue(convertedMap, fieldValue);
        }
        return convertedMap;
    }

    public static List<EntityFieldValue> logicalKeyFromStrings(Entity entity, String... keys) {
        Entity.LogicalKey logicalKey = entity.getLogicalKey();
        List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
        assert keys.length == logicalKeyList.size();
        List<EntityFieldValue> lkFieldValues = new ArrayList<>();
        for (int i = 0; i < logicalKeyList.size(); i++) {
            EntityField field = logicalKeyList.get(i);
            String lkElem = keys[i];
            Object convertedLkElem = FieldConverters.getConvertedFieldValue(field, lkElem);
            lkFieldValues.add(EntityFieldValue.create(field, convertedLkElem));
        }
        return lkFieldValues;
    }

    public static String toFieldName(Field field) {
        return field.getName();
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
            case PASSWORD:
                return null;
            case RECORD:
                return new Object();
            case ENTITY_EMBEDED:
                return null;
            case TEXT_ARRAY:
                return new String[]{};
        }
        throw new RuntimeException(String.format("No default found for type %s", field));
    }
}
