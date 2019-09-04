package it.at7.gemini;

import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
/**
 * UnitTestBase provides the entry point to full Gemini Wapp Test.
 */
public abstract class UnitTestBase {
    private static boolean contextInitialized = false;

    //==== GEMINI TEST PREAMBOLE - WEBAPP APPLICANTION CONTEXT ====/
    public static MockMvc mockMvc;
    public static ConfigurableApplicationContext webApp; // web app may be not initialized
    public static ConfigurableApplicationContext parentContext; // parent context is always initialized

    @Before
    public void setup() throws GeminiException {
        if (contextInitialized)
            return;
        parentContext = getApplicationContext();
        if (initializeWebApp())
            setupWebMockMvc(parentContext);
        contextInitialized = true;
    }

    protected abstract ConfigurableApplicationContext getApplicationContext() throws GeminiException;

    public boolean initializeWebApp() {
        return true;
    }

    public boolean initializeSecurity() {
        return false;
    }

    @AfterClass
    public static void clean() {
        if (webApp != null) {
            ConfigurableApplicationContext parent = (ConfigurableApplicationContext) webApp.getParent();
            parent.close();
            parentContext.close();
            webApp.close();
        } else {
            parentContext.close();
        }
        contextInitialized = false;
    }

    public void setupWebMockMvc(ConfigurableApplicationContext wApp) {
        webApp = wApp;
        DefaultMockMvcBuilder mockMvcBuilder = webAppContextSetup((WebApplicationContext) wApp);
        if (initializeSecurity())
            mockMvcBuilder = mockMvcBuilder.apply(springSecurity());
        mockMvc = mockMvcBuilder.build();
    }
    //=============================================================/


    public static String API_PATH = "/api";
}
