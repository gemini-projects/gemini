package it.at7.gemini.performanceTest;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.SchemaManager;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.TransactionManager;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GeminiPostgresqlTestBase {
    public static SchemaManager schemaManager;
    public static TransactionManager transactionManager;
    public static PersistenceEntityManager persistenceEntityManager;
    public static ConfigurableApplicationContext contex;

    @BeforeClass
    public static void initializeTest() {
        contex = IntegrationTestMain.initializeGemini();
        schemaManager = Services.getSchemaManager();
        assertNotNull(schemaManager);
        transactionManager = Services.getTransactionManager();
        assertNotNull(transactionManager);
        persistenceEntityManager = Services.getPersistenceEntityManager();
        assertNotNull(persistenceEntityManager);
    }

    @AfterClass
    public static void after() {
        if (contex != null) contex.close();
    }

}
