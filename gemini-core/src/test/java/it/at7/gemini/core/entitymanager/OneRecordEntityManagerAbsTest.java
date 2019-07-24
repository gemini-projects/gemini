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

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class OneRecordEntityManagerAbsTest extends UnitTestNoMockWeb {
    public static String SINGLETON_ENTITY = "SingletonTest";

    @Test
    public void n1_getSingleRecord() throws GeminiException {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity e = schemaManager.getEntity(SINGLETON_ENTITY);
        EntityManager entityManager = Services.getEntityManager();
        EntityRecord record = entityManager.getSingleEntityRecord(e);
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
        EntityManager entityManager = Services.getEntityManager();
        Entity e = Services.getSchemaManager().getEntity(SINGLETON_ENTITY);
        EntityRecord record = entityManager.getSingleEntityRecord(e);
        record.put("text", "IntegTest");
        EntityRecord rupdt = entityManager.putOrUpdate(record);
        assertEquals(record.getID(), rupdt.getID());
        assertEquals("IntegTest", rupdt.get("text"));
        record.put("text", "IntegTest-update");
        rupdt = entityManager.update(record);
        assertEquals(record.getID(), rupdt.getID());
        assertEquals("IntegTest-update", rupdt.get("text"));
    }

    @Test(expected = GeminiException.class)
    public void n4_deleteSingleRecordShouldThrowException() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        Entity e = Services.getSchemaManager().getEntity(SINGLETON_ENTITY);
        EntityRecord record = entityManager.getOneRecordEntity(e);
        entityManager.delete(record);
    }

}
