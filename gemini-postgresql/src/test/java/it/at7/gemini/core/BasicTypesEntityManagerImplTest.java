package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.BasicTypesEntityManagerAbstTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class BasicTypesEntityManagerImplTest extends BasicTypesEntityManagerAbstTest {
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

}
