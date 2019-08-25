package it.at7.gemini.auth.events;

import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.events.BeforeInsertField;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import org.springframework.util.StringUtils;

@Events(entityName = UserRef.NAME, order = -100)
public class UserEvents {

    @BeforeInsertField(field = "displayName")
    public String defaultDisplayName(EventContext context) {
        EntityRecord entityRecord = context.getEntityRecord();
        String displayName = entityRecord.get("displayName");
        if (StringUtils.isEmpty(displayName)) {
            return entityRecord.get("username");
        }
        return displayName;
    }
}
