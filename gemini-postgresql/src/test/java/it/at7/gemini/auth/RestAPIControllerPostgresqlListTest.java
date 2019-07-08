package it.at7.gemini.auth;

import it.at7.gemini.api.RestAPIControllerListTest;
import it.at7.gemini.boot.IntegrationTestMain;
import org.springframework.context.ConfigurableApplicationContext;

public class RestAPIControllerPostgresqlListTest extends RestAPIControllerListTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeFullIntegrationWebApp();
    }
}
