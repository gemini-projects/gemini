package it.at7.gemini.core;

import it.at7.gemini.core.events.BeforeInsertField;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.schema.Entity;

import java.time.LocalDateTime;


@Events(entityName = Entity.CORE_META, order = -100)
public class CoreMetaEvents {

    @BeforeInsertField(field = "created")
    public LocalDateTime beforeInsertCreted() {
        System.out.println("yeaahhh befooore");
        return LocalDateTime.now();
    }
}
