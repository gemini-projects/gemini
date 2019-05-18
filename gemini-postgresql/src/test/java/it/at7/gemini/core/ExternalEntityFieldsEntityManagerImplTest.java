package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.ExternalEntityFieldsEntityManagerAbstTest;
import org.springframework.context.ConfigurableApplicationContext;

public class ExternalEntityFieldsEntityManagerImplTest extends ExternalEntityFieldsEntityManagerAbstTest {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini();
    }

}
