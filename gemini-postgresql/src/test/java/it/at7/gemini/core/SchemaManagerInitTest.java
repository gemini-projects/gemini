package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.schemamanager.SchemaManagerInitAbstTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class SchemaManagerInitTest extends SchemaManagerInitAbstTest {
    static ConfigurableApplicationContext contex;

    @BeforeClass
    public static void initializeTest() {
        contex = IntegrationTestMain.initializeOnlyServices();
    }

    @AfterClass
    public static void after() {
        contex.close();
    }
}
