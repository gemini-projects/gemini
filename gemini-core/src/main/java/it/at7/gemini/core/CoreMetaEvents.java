package it.at7.gemini.core;

import it.at7.gemini.core.events.BeforeInsertField;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.core.events.OnUpdateField;
import it.at7.gemini.schema.Entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Events(entityName = Entity.CORE_META_INTERFACE, order = -100)
public class CoreMetaEvents {

    @BeforeInsertField(field = "created")
    @BeforeInsertField(field = "modified")
    @OnUpdateField(field = "modified")
    public LocalDateTime transactionDate(EventContext context) {
        if (context.getTransaction().isPresent()) {
            return context.getTransaction().get().getOpenTime();
        }
        return LocalDateTime.now(ZoneOffset.UTC);
    }

}
