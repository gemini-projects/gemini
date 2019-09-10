package it.at7.gemini.boot;

import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class SuiteTestCoreJunitINIT {
    public static ConfigurableApplicationContext applictionContext;

    @BeforeClass
    public static void setup() throws GeminiException {
        applictionContext = IntegrationTestMain.initializeGemini();
    }

    @AfterClass
    public static void clean() {
        applictionContext.close();

    }
}
