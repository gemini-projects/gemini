package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.ClosedDomainEntityManagerAbstTest;
import org.springframework.context.ConfigurableApplicationContext;

public class ClosedDomainEntityManagerTest extends ClosedDomainEntityManagerAbstTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini();
    }

}
