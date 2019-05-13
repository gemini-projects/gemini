package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.EmbedableTypeEntityManagerAbsTest;
import org.springframework.context.ConfigurableApplicationContext;

public class EmbedableTypeEntityManagerImplTest extends EmbedableTypeEntityManagerAbsTest {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
    }

}
