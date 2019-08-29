package it.at7.gemini.gui.events;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.events.BeforeInsertField;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.FieldRef;

@Events(entityName = FieldRef.NAME, order = -100)
public class FieldGUIEvents {

    @BeforeInsertField(field = "displayName")
    public String initDisplayName(EventContext eventContext) throws GeminiException {
        EntityRecord entityRecord = eventContext.getEntityRecord();
        String name = entityRecord.get(FieldRef.FIELDS.NAME);
        assert name != null;
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
