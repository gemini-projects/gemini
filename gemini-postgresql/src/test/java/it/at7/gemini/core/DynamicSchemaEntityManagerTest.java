package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityRef;
import org.junit.Test;

public class DynamicSchemaEntityManagerTest {

    @Test(expected = GeminiException.class)
    public void shouldTrhowExceptionOnEntityCreateWhenDynamicSchemaDefault() throws GeminiException {
        putEntityRecordForENTITY("excption_on_default");
    }

    // TODO - Dynamic Entity Schema - add check when dynamic schema is enabled --- move on core ??

    private void putEntityRecordForENTITY(String name) throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        SchemaManager schemaManager = Services.getSchemaManager();

        Entity entity = schemaManager.getEntity(EntityRef.NAME);
        EntityRecord entityRecord = new EntityRecord(entity);
        entityRecord.set(EntityRef.FIELDS.NAME, name);
        entityManager.putIfAbsent(entityRecord);
    }
}
