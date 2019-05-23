package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.PasswordEntityManagerAbstTest;
import org.springframework.context.ConfigurableApplicationContext;

public class PasswordEntityManagerImplTest extends PasswordEntityManagerAbstTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini();
    }

}
