package it.at7.gemini.api;

import it.at7.gemini.boot.IntegrationTestMain;
import org.springframework.context.ConfigurableApplicationContext;

public class ClosedDomainRESTAPIControllerTest extends ClosedDomainRESTAPIControllerAbstTest {
    @Override
    public ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeFullIntegrationWebApp();
    }
}
