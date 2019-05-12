package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.FilterEntityManagerAbsTest;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class FilterEntityManagerTest extends FilterEntityManagerAbsTest {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() throws GeminiException {
        return IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
    }
}
