package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class TransactionManagerPGTest extends TransactionManagerImplTest {

    static ConfigurableApplicationContext contex;

    @BeforeClass
    public static void initializeTest() {
        contex = IntegrationTestMain.initializeGemini();
    }

    @AfterClass
    public static void after() {
        if (contex != null)
            contex.close();
    }

}
