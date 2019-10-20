package it.at7.gemini.gui.schema;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;

public interface EntityGUIRef {
    String NAME = "ENTITYGUI";

    class FIELDS {
        public static final String ENTITY = "entity";
        public static final String DISPLAY_NAME = "displayName";
    }

    static EntityRecord record(String entity, String displayName) {
        EntityRecord guiRef = new EntityRecord(Services.getEntityManager().getEntity(NAME));
        guiRef.put(FIELDS.ENTITY, entity.toUpperCase());
        guiRef.put(FIELDS.DISPLAY_NAME, displayName);
        return guiRef;
    }
}
