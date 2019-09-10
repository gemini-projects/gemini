package it.at7.gemini.api;

import it.at7.gemini.boot.IntegrationTestMain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SuiteTestWebApiINIT {

    private static ConfigurableApplicationContext webApp; // web app may be not initialized
    private static ConfigurableApplicationContext parentContext; // parent context is always initialized

    @BeforeClass
    public static void setup() {
        parentContext = IntegrationTestMain.initializeFullIntegrationWebApp();
        setupWebMockMvc(parentContext);
    }

    @AfterClass
    public static void clean() {
        ConfigurableApplicationContext parent = (ConfigurableApplicationContext) webApp.getParent();
        parent.close();
        parentContext.close();
        webApp.close();
    }

    public static void setupWebMockMvc(ConfigurableApplicationContext wApp) {
        webApp = wApp;
        DefaultMockMvcBuilder mockMvcBuilder = webAppContextSetup((WebApplicationContext) wApp);
      /*  if (USE_SECURITY)
            mockMvcBuilder = mockMvcBuilder.apply(springSecurity());*/
        MockMVCUtils.mockMvc = mockMvcBuilder.build();
    }
}
