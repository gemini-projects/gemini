package it.at7.gemini.boot;

import it.at7.gemini.api.Api;
import it.at7.gemini.core.Gemini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

@Component
public class IntegrationTestMain {
    Logger logger = LoggerFactory.getLogger(IntegrationTestMain.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void preliminarOperations() throws SQLException, IOException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        eraseDatabase(statement);
        connection.commit();
        connection.close();
    }

    private void eraseDatabase(Statement statement) throws SQLException, IOException {
        logger.info("Initialization: deleting schema");
        Resource resource = applicationContext.getResource("classpath:erasePublicSchema");
        String eraseDBSql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        int i = statement.executeUpdate(eraseDBSql);
        logger.info("Initialization: deleting schema - qrs: " + i);
    }


    private static void applicationContextForPreliminarOperation() {
        new SpringApplicationBuilder()
                .sources(Autoconfiguration.class, IntegrationTestMain.class).web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run().close();
    }


    private static ConfigurableApplicationContext loadRealAppApplicationContext(Class... classes) {
        return new SpringApplicationBuilder()
                .parent(Autoconfiguration.class, Gemini.class).web(WebApplicationType.NONE)
                .sources(classes)
                .bannerMode(Banner.Mode.OFF)
                .run();
    }

    public static ConfigurableApplicationContext startSpring(Class... classes) {
        applicationContextForPreliminarOperation();
        return loadRealAppApplicationContext(classes);
    }

    /**
     * Fully initialize Gemini as a normal start
     */
    public static ConfigurableApplicationContext initializeGemini(Class... classes) {
        ConfigurableApplicationContext context = startSpring(classes);
        Gemini gemini = context.getBean(Gemini.class);
        gemini.init();
        return context;
    }

    /**
     * Initialize Services for Integration Test Withou Gemini Init
     */
    public static ConfigurableApplicationContext initializeOnlyServices(Class... classes) {
        ConfigurableApplicationContext context = startSpring(classes);
        return context;
    }

    public static ConfigurableApplicationContext initializeFullIntegrationWebApp() {
        return initializeFullIntegrationWebApp(Set.of(), Set.of());
    }

    public static ConfigurableApplicationContext initializeFullIntegrationWebApp(Set<Class> coreBean, Set<Class> apiBean) {
        ConfigurableApplicationContext root = initializeGemini(coreBean.toArray(new Class[0]));
        ConfigurableApplicationContext webApp = new SpringApplicationBuilder()
                .parent(root).sources(Api.class, Autoconfiguration.class).sources(apiBean.toArray(new Class[0]))
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .run();
        return webApp;
    }


    @EnableAutoConfiguration
    public static class Autoconfiguration {
    }

    @EnableAutoConfiguration
    @ComponentScan(value = {"it.at7.gemini.core"}, excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = Gemini.class)})
    public static class AutoconfigurationNoGemini {
    }
}
