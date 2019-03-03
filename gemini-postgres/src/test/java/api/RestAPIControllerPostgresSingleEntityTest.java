package api;

import it.at7.gemini.api.RestAPIControllerSingleEntityTest;
import it.at7.gemini.boot.IntegrationTestMain;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;

public class RestAPIControllerPostgresSingleEntityTest extends RestAPIControllerSingleEntityTest {

    @Override
    public ConfigurableApplicationContext getApplicationContext() {
        return IntegrationTestMain.initializeFullIntegrationWebApp();
    }

}
