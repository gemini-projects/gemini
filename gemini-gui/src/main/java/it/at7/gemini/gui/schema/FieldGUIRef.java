package it.at7.gemini.gui.schema;

import it.at7.gemini.core.*;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.FieldRef;

import java.util.Map;

public interface FieldGUIRef {
    String NAME = "FIELDGUI";

    class FIELDS {
        public static final String FIELD = "field";
        public static final String DISPLAY_NAME = "displayName";
    }

    static EntityRecord record(String entity, String field, String displayName) {
        EntityManager entityManager = Services.getEntityManager();
        assert entityManager != null;
        EntityRecord guiRef = new EntityRecord(entityManager.getEntity(NAME));
        Entity FIELD = entityManager.getEntity(FieldRef.NAME);
        assert FIELD != null;
        EntityReferenceRecord fieldRef = FieldConverters.logicalKeyFromObject(FIELD,
                Map.of(FieldRef.FIELDS.ENTITY, entity.toUpperCase(),
                        FieldRef.FIELDS.NAME, field));
        guiRef.put(FIELDS.FIELD, fieldRef);
        guiRef.put(FIELDS.DISPLAY_NAME, displayName);
        return guiRef;
    }
}
