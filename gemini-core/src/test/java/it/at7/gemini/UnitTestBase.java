package it.at7.gemini;

import it.at7.gemini.api.Api;
import it.at7.gemini.core.Gemini;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class UnitTestBase {
    public static boolean contextInitialized = false;

    //==== GEMINI TEST PREAMBOLE - WEBAPP APPLICANTION CONTEXT ====/
    public static MockMvc mockMvc;
    public static ConfigurableApplicationContext webApp; // web app must me initialized in implementation tests


    @Before
    public void setup() throws SQLException, GeminiException {
        if (contextInitialized)
            return;
        ConfigurableApplicationContext context = getApplicationContext();
        setupWebMockMvc(context);
        contextInitialized = true;
    }

    protected abstract ConfigurableApplicationContext getApplicationContext();

    @AfterClass
    public static void clean() {
        if (webApp != null) {
            ConfigurableApplicationContext parent = (ConfigurableApplicationContext) webApp.getParent();
            parent.close();
            webApp.close();
            contextInitialized = false;
        }
    }

    public void setupWebMockMvc(ConfigurableApplicationContext wApp) {
        webApp = wApp;
        mockMvc = webAppContextSetup((WebApplicationContext) wApp).build();
    }
    //=============================================================/


    public static String API_PATH = "/api";

    public static ConfigurableApplicationContext setupFullWebAPP(Class... classes) throws SQLException, GeminiException {
        ConfigurableApplicationContext root = new SpringApplicationBuilder()
                .parent(Gemini.class, UnitTestConfiguration.class).web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run();
        root.setId("Root");
        Gemini gemini = root.getBean(Gemini.class);
        gemini.init();


        ConfigurableApplicationContext webApp = new SpringApplicationBuilder()
                .parent(root).sources(Api.class, UnitTestConfiguration.class)
                .sources(classes).web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .run();

        return webApp;
    }
}
