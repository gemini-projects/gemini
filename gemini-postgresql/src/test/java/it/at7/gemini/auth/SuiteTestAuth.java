package it.at7.gemini.auth;

import it.at7.gemini.api.AuthEventsAPIAbstTest;
import it.at7.gemini.api.LoginAndBearerAbstTest;
import it.at7.gemini.api.MockMVCUtils;
import it.at7.gemini.boot.IntegrationTestMain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AuhtModuleAbstTest.class,
        AuthMetaEventsAbstTest.class,
        LoginAndBearerAbstTest.class,
        AuthEventsAPIAbstTest.class
})
public class SuiteTestAuth {

    // ENABLED AUTHENTICATION MODULES AND SPRING SECURITY FEATURES

    private static ConfigurableApplicationContext webApp; // web app may be not initialized
    private static ConfigurableApplicationContext parentContext; // parent context is always initialized

    @BeforeClass
    public static void setup() {
        parentContext = IntegrationTestMain.initializeFullIntegrationWebApp(Set.of(AuthModule.class), Set.of(AuthModuleAPI.class));
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
        mockMvcBuilder = mockMvcBuilder.apply(springSecurity());
        MockMVCUtils.mockMvc = mockMvcBuilder.build();
    }

}
