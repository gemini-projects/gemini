package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.SchemaManager;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Test;

public class OneRecordEntityManagerAbsTest {

    @Test
    public void n1_putIfAbsent() throws GeminiException {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity e = schemaManager.getEntity("SingletonTest");
        EntityManager entityManager = Services.getEntityManager();
        entityManager.getRecord(e);
    }

}
