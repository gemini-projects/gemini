package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.conf.DynamicSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.SchemaException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

public class DynamicSchemaEntityManagerTest {
    static ConfigurableApplicationContext contex;

    @BeforeClass
    public static void initializeTest() {
        contex = IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
    }

    @AfterClass
    public static void after() {
        if (contex != null)
            contex.close();
    }

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
