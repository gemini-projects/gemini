package it.at7.gemini.core.persistence;

import it.at7.gemini.schema.FieldType;

public class FieldTypePersistenceUtility {

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
            case TRANSL_TEXT:
            case TEXT_ARRAY:
                return true;
            case ENTITY_REF:
            case GENERIC_ENTITY_REF:
            case RECORD:
            case ENTITY_COLLECTION_REF:
                return false;
        }
        return false;
    }
}
