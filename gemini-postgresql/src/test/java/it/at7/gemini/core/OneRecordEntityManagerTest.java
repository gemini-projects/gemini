package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.OneRecordEntityManagerAbsTest;
import org.springframework.context.ConfigurableApplicationContext;

public class OneRecordEntityManagerTest extends OneRecordEntityManagerAbsTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini();
    }

}
