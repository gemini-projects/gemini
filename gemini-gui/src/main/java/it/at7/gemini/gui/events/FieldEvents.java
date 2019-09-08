package it.at7.gemini.gui.events;

import it.at7.gemini.core.*;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.core.events.OnRecordInserted;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.gui.schema.FieldGUIRef;
import it.at7.gemini.schema.FieldRef;

import java.util.Optional;

@Events(entityName = FieldRef.NAME, order = -100)
public class FieldEvents {

    private final EntityManager entityManager;

    public FieldEvents(EntityManager entityManager) {
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
        String name = entityRecord.get(FieldRef.FIELDS.NAME);
        EntityRecord guiRef = new EntityRecord(this.entityManager.getEntity(FieldGUIRef.NAME));
        guiRef.put(FieldGUIRef.FIELDS.FIELD, EntityReferenceRecord.fromEntityRecord(entityRecord));
        guiRef.put(FieldGUIRef.FIELDS.DISPLAY_NAME, name);
        this.entityManager.putIfAbsent(guiRef, opContext, t);
    }
}
