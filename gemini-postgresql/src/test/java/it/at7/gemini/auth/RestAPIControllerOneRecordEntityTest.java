package it.at7.gemini.auth;

import it.at7.gemini.api.RestAPIControllerOneRecordEntityAbstTest;
import it.at7.gemini.boot.IntegrationTestMain;
import org.springframework.context.ConfigurableApplicationContext;

public class RestAPIControllerOneRecordEntityTest extends RestAPIControllerOneRecordEntityAbstTest {
    @Override
    public ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeFullIntegrationWebApp();
    }
}
