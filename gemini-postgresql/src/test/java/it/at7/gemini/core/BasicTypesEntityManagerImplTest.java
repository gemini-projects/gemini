package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.BasicTypesEntityManagerAbstTest;
import org.springframework.context.ConfigurableApplicationContext;

public class BasicTypesEntityManagerImplTest extends BasicTypesEntityManagerAbstTest {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini();
    }

}
