package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.FilterEntityManagerAbsTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class FilterEntityManagerTest extends FilterEntityManagerAbsTest {

    static ConfigurableApplicationContext contex;

    @BeforeClass
    public static void initializeTest() {
        contex = IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
    }

    @AfterClass
    public static void after() {
        contex.close();
    }
}
