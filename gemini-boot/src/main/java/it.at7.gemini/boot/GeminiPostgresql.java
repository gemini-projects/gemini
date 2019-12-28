package it.at7.gemini.boot;

import it.at7.gemini.api.Api;
import it.at7.gemini.auth.AuthModule;
import it.at7.gemini.auth.AuthModuleAPI;
import it.at7.gemini.core.Gemini;
import it.at7.gemini.gui.GuiAPI;
import it.at7.gemini.gui.GuiModule;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;


public class GeminiPostgresql {
    private static final Logger logger = LoggerFactory.getLogger(GeminiPostgresql.class);

    public static void start(String[] args) {
        start(args, Set.of(), Set.of());
    }

    public static void start(String[] args, Set<Class> coreBean, Set<Class> apiBean) {
        logger.info("***** STARTING GEMINI POSTRESQL MAIN *****");

        ConfigurableApplicationContext root = getGeminiRootContext(args, coreBean);

        logger.info("STARTING - GEMINI-WEBAPP CONTEXT ");
        SpringApplicationBuilder webAppBuilder = new SpringApplicationBuilder()
                .parent(root).sources(Api.class).sources(GuiAPI.class, AuthModuleAPI.class, AutoConfiguration.class).web(WebApplicationType.SERVLET);
        if (apiBean.size() != 0) {
            webAppBuilder.sources(apiBean.toArray(new Class[0]));
        }
        ConfigurableApplicationContext gui = webAppBuilder.bannerMode(Banner.Mode.OFF)
                .run(args);
        gui.setId("GEMINI-WAPP");
        logger.info("STARTED - GEMINI-WEBAPP CONTEXT");
    }

    public static void startfor3Party(String[] args, Set<Class> coreBean, Set<Class> childContextBean) {
        logger.info("***** STARTING GEMINI POSTRESQL 3PARTY MAIN *****");
        ConfigurableApplicationContext root = getGeminiRootContext(args, coreBean);

        logger.info("STARTING - GEMINI-CHILD CONTEXT ");
        SpringApplicationBuilder webAppBuilder = new SpringApplicationBuilder()
                .parent(root).sources(AutoConfiguration.class).web(WebApplicationType.NONE);
        if (childContextBean.size() != 0) {
            webAppBuilder.sources(childContextBean.toArray(new Class[0]));
        }
        ConfigurableApplicationContext gui = webAppBuilder.bannerMode(Banner.Mode.OFF)
                .run(args);
        gui.setId("GEMINI-CHILD");
        logger.info("STARTED - GEMINI-CHILD CONTEXT");
    }

    @NotNull
    private static ConfigurableApplicationContext getGeminiRootContext(String[] args, Set<Class> coreBean) {
        logger.info("STARTING - GEMINI-ROOT CONTEXT ");
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder()
                .parent(AutoConfiguration.class, Gemini.class, AuthModule.class, GuiModule.class);
        if (coreBean.size() != 0) {
            appBuilder.sources(coreBean.toArray(new Class[0]));
        }
        ConfigurableApplicationContext root = appBuilder
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        root.setId("GEMINI-ROOT");
        Gemini gemini = root.getBean(Gemini.class);
        gemini.init();
        logger.info("STARTED - GEMINI-ROOT CONTEXT");
        return root;
    }

    public static ConfigurableApplicationContext syncRootContext(String[] args, Set<Class> coreBean) {
        return getGeminiRootContext(args, coreBean);
    }

    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
    public static class AutoConfiguration {
    }

}
