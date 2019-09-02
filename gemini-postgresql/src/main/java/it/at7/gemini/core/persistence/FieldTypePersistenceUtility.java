package it.at7.gemini.core.persistence;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class FieldTypePersistenceUtility {
    private static final String META_PREFIX = "_meta_";
    private static final String ENTITY_PREFIX = "_entity_";
    private static final String REF_PREFIX = "_ref_";

    private static Map<Long, Entity> entities;

    public static void initEntities(Collection<Entity> allEntities) {
        entities = allEntities.stream().collect(Collectors.toMap(e -> (Long) e.getIDValue(), Function.identity()));
    }

    public static boolean oneToOneType(FieldType type) {
        switch (type) {
            case PK:
            case TEXT:
            case NUMBER:
            case LONG:
            case DOUBLE:
            case BOOL:
            case TIME:
            case DATE:
            case DATETIME:
            case TEXT_ARRAY:
                return true;
            case ENTITY_REF:
            case ENTITY_EMBEDED:
            case GENERIC_ENTITY_REF:
            case RECORD:
            case PASSWORD:
                return false;
        }
        return false;
    }


    public static boolean entityType(FieldType type) {
        switch (type) {
            case PK:
            case TEXT:
            case NUMBER:
            case LONG:
            case DOUBLE:
            case BOOL:
            case TIME:
            case DATE:
            case DATETIME:
            case TEXT_ARRAY:
            case RECORD:
                return false;
            case ENTITY_REF:
            case ENTITY_EMBEDED:
            case ENTITY_REF_ARRAY:
                return true;
        }
        return false;
    }

    public static boolean passwordType(FieldType type) {
        return type == FieldType.PASSWORD;
    }

    public static boolean genericEntityRefType(FieldType type) {
        return type == FieldType.GENERIC_ENTITY_REF;
    }

    public static String pkDomainArrayFromEntity(String entityName) {
        return pkForeignKeyDomainFromEntity(entityName) + "[]";
    }

    public static Entity getEntityByID(Long entityIDValue) {
        return entities.get(entityIDValue);
    }

    public static String pkForeignKeyDomainFromEntity(Entity entity) {
        return entity.getName().toLowerCase() + "_pk";
    }

    public static String pkForeignKeyDomainFromEntity(String entityName) {
        return entityName.toLowerCase() + "_pk";
    }

    public static String genericRefEntityFieldName(EntityField field, boolean wrap) {
        String fieldName = ENTITY_PREFIX + fieldName(field, false);
        if (wrap) {
            return wrapDoubleQuotes(fieldName);
        }
        return fieldName;
    }

    public static String genericRefActualRefFieldName(EntityField field, boolean wrap) {
        String fieldName = REF_PREFIX + fieldName(field, false);
        if (wrap) {
            return wrapDoubleQuotes(fieldName);
        }
        return fieldName;
    }

    public static String wrapDoubleQuotes(String st) {
        return '"' + st + '"';
    }

    public static String fieldName(EntityField field, boolean wrap) {
        String prefix = field.getScope().equals(EntityField.Scope.META) ? META_PREFIX : "";
        String name = prefix + field.getName().toLowerCase();
        if (wrap)
            return wrapDoubleQuotes(name);
        return name;
    }
}
