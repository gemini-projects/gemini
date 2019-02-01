package api;

import it.at7.gemini.api.RestAPIControllerSingleEntityTest;
import it.at7.gemini.boot.IntegrationTestMain;
import org.junit.BeforeClass;

public class RestAPIControllerPostgresSingleEntityTest extends RestAPIControllerSingleEntityTest {
    @BeforeClass
    public static void setup() {
        setupWebMockMvc(IntegrationTestMain.initializeFullIntegrationWebApp());
    }
}
