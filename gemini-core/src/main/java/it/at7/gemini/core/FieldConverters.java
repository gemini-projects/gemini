package it.at7.gemini.core;

import it.at7.gemini.core.type.Password;
import it.at7.gemini.exceptions.InvalidLogicalKeyValue;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static it.at7.gemini.core.utils.DateTimeUtility.*;

public class FieldConverters {
    private static Logger logger = LoggerFactory.getLogger(FieldConverters.class);


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
                return stValue;
            case PASSWORD:
                if (String.class.isAssignableFrom(objValue.getClass())) {
                    return new Password(stValue);
                }
                if (Password.class.isAssignableFrom(objValue.getClass())) {
                    return objValue;
                }
                break;
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
                if (OffsetTime.class.isAssignableFrom(objValue.getClass())) {
                    return objValue;
                }
                return isoStringToLocalTime(stValue);
            case DATE:
                if (LocalDate.class.isAssignableFrom(objValue.getClass())) {
                    return objValue;
                }
                return isoStringToLocalDate(stValue);
            case DATETIME:
                if (LocalDateTime.class.isAssignableFrom(objValue.getClass())) {
                    return objValue;
                }
                return isoStringToLocalDateTime(stValue);
            case ENTITY_REF:
                if (EntityReferenceRecord.class.isAssignableFrom(objValue.getClass())) {
                    // no need to convert
                    return objValue;
                }
                EntityReferenceRecord pkValue;
                if (EntityRecord.class.isAssignableFrom(objValue.getClass())) {
                    EntityRecord entityRecord = (EntityRecord) objValue;
                    Entity fieldEntity = field.getEntityRef();
                    Entity objValueEntity = entityRecord.getEntity();
                    assert fieldEntity != null && fieldEntity.equals(objValueEntity);
                    pkValue = logicalKeyFromEntityRecord(entityRecord);
                    // TODO BOO sbagliato qua misa... meglio create il ref anche con l'oggetto di partenza
                } else {
                    pkValue = logicalKeyFromObject(field.getEntityRef(), objValue);
                }
                assert pkValue != null;
                return pkValue;
            case ENTITY_EMBEDED:
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
            case ENTITY_REF_ARRAY:
                if (Collection.class.isAssignableFrom(objValue.getClass())) {
                    Collection<Object> genColl = (Collection) objValue;
                    if (genColl.isEmpty()) {
                        return genColl;
                    }
                    Object[] elements = genColl.toArray();
                    Object firstElem = elements[0];
                    Class<?> firstElementClass = firstElem.getClass();
                    if (EntityRecord.class.isAssignableFrom(firstElementClass) ||
                            EntityReferenceRecord.class.isAssignableFrom(firstElementClass) ||
                            String.class.isAssignableFrom(firstElementClass)
                    ) {
                        return objValue;
                    }
                }
                throw new RuntimeException(String.format("Field %s must have an EntityRecord|EntityReferenceRecord collection", field.toString()));
            case GENERIC_ENTITY_REF:
                if (EntityReferenceRecord.class.isAssignableFrom(objValue.getClass())) {
                    // no need to convert
                    return objValue;
                }
                if (EntityRecord.class.isAssignableFrom(objValue.getClass())) {
                    EntityRecord entityRecord = (EntityRecord) objValue;
                    pkValue = createEntityReferenceRecordFromER(entityRecord);
                    // TODO BOO sbagliato qua misa... meglio create il ref anche con l'oggetto di partenza
                } else {
                    throw new RuntimeException(String.format("Field %s must have an EntityRecord|EntityReferenceRecord object to identify the right entity reference", field.toString()));
                }
                assert pkValue != null;
                return pkValue;
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
    
    @NotNull
    public static EntityReferenceRecord createEntityReferenceRecordFromER(EntityRecord entityRecord) {
        return createEntityReferenceRecordFromER(entityRecord.getEntity(), entityRecord.getID(), entityRecord);
    }

    @NotNull
    public static EntityReferenceRecord createEntityReferenceRecordFromER(Entity entity, Object pkValue, EntityRecord lkEntityRecord) {
        EntityReferenceRecord entityReferenceRecord;
        entityReferenceRecord = new EntityReferenceRecord(entity);
        entityReferenceRecord.addPKValue(pkValue);

        // TODO put the entity instead of the entityreference ??
        for (Field entityLkField : entity.getLogicalKey().getLogicalKeyList()) {
            entityReferenceRecord.addLogicalKeyValue(entityLkField, lkEntityRecord.get(entityLkField));
        }
        return entityReferenceRecord;
    }
}
