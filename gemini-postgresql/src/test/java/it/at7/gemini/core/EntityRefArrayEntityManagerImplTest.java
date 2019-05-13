package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.EntityRefArrayEntityManagerAbsTest;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.context.ConfigurableApplicationContext;

public class EntityRefArrayEntityManagerImplTest extends EntityRefArrayEntityManagerAbsTest {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() throws GeminiException {
        ConfigurableApplicationContext context = IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
        insertDomainRecords();
        return context;
    }

}
