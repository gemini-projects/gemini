package it.at7.gemini.core.entitymanager;

import it.at7.gemini.UnitTestNoMockWeb;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.SchemaManager;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class OneRecordEntityManagerAbsTest extends UnitTestNoMockWeb {
    public static String SINGLETON_ENTITY = "SingletonTest";

    @Test
    public void n1_getSingleRecord() throws GeminiException {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity e = schemaManager.getEntity(SINGLETON_ENTITY);
        EntityManager entityManager = Services.getEntityManager();
        EntityRecord record = entityManager.getRecord(e);
        assert record != null;
    }

    @Test(expected = GeminiException.class)
    public void n2_postSingleRecordShouldThrowException() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        Entity e = Services.getSchemaManager().getEntity(SINGLETON_ENTITY);
        EntityRecord record = new EntityRecord(e);
        record.put("text", "IntegTest");
        entityManager.putIfAbsent(record);
    }

    @Test
    public void n3_putSingleRecord() throws GeminiException {
        // TODO putorupdate / update
    }

    @Test
    public void n4_deleteSingleRecordShouldThrowException() throws GeminiException {
        // TODO
    }

}
