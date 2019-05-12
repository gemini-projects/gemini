package it.at7.gemini.core;

import it.at7.gemini.boot.IntegrationTestMain;
import it.at7.gemini.core.entitymanager.EmbedableTypeEntityManagerAbsTest;
import it.at7.gemini.core.entitymanager.EntityRefEntityManagerAbstTest;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;

public class EmbedableTypeEntityManagerImplTest extends EmbedableTypeEntityManagerAbsTest {

    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeGemini(IntegrationTestModule.class);
    }

}
