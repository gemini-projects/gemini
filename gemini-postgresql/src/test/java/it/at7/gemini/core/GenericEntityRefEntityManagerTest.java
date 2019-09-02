package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.GenericEntityRefEntityManagerAbstTest;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.context.ConfigurableApplicationContext;

public class GenericEntityRefEntityManagerTest extends GenericEntityRefEntityManagerAbstTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() throws GeminiException {
        return IntegrationTestMain.initializeGemini();
    }
}
