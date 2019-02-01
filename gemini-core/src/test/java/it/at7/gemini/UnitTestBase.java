package it.at7.gemini;

import it.at7.gemini.api.Api;
import it.at7.gemini.core.Gemini;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnitTestBase {
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
