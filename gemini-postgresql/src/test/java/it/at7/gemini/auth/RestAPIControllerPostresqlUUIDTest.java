package it.at7.gemini.auth;

import it.at7.gemini.api.RestAPIControllerUUIDTest;
import it.at7.gemini.boot.IntegrationTestMain;
import org.springframework.context.ConfigurableApplicationContext;

public class RestAPIControllerPostresqlUUIDTest extends RestAPIControllerUUIDTest {
    @Override
    protected ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeFullIntegrationWebApp();
    }
}
