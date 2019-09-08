package it.at7.gemini.gui.events;

import it.at7.gemini.core.*;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.core.events.OnRecordInserted;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.gui.schema.EntityGUIRef;
import it.at7.gemini.schema.EntityRef;

import java.util.Optional;

@Events(entityName = EntityRef.NAME, order = -100)
public class EntityEvents {

    private final EntityManager entityManager;

    public EntityEvents(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @OnRecordInserted
    public void onRecordInserted(EventContext eventContext) throws GeminiException {
        Optional<Transaction> transaction = eventContext.getTransaction();
        Optional<EntityOperationContext> entityOperationContext = eventContext.getEntityOperationContext();
        assert transaction.isPresent();
        assert entityOperationContext.isPresent();
        Transaction t = transaction.get();
        EntityOperationContext opContext = entityOperationContext.get();

        EntityRecord entityRecord = eventContext.getEntityRecord();
        String name = entityRecord.get(EntityRef.FIELDS.NAME);
        EntityRecord guiRef = new EntityRecord(this.entityManager.getEntity(EntityGUIRef.NAME));
        guiRef.put(EntityGUIRef.FIELDS.ENTITY, EntityReferenceRecord.fromEntityRecord(entityRecord));
        guiRef.put(EntityGUIRef.FIELDS.DISPLAY_NAME, name);
        this.entityManager.putIfAbsent(guiRef, opContext, t);
    }
}
