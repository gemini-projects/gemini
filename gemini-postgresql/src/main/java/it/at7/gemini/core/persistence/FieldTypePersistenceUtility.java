package it.at7.gemini.core.persistence;

import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Optional;

public class FieldTypePersistenceUtility {
    public static final String META_PREFIX = "_meta_";
    public static final String ENTITY_PREFIX = "_entity_";
    public static final String REF_PREFIX = "_ref_";

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

    @Nullable
    public static Optional<Entity> getEntityByID(Collection<Entity> allEntities, Long idValue) {
        return allEntities.stream().filter(e -> e.getIDValue() == idValue).findFirst();
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
