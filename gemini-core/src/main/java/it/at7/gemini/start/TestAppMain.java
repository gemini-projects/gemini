package it.at7.gemini.start;

import it.at7.gemini.api.Api;
import it.at7.gemini.core.Gemini;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan("it.at7.gemini")
@SpringBootApplication
public class TestAppMain {

    public static void main(String[] args) {

        ConfigurableApplicationContext root = new SpringApplicationBuilder()
                .parent(Gemini.class, TestAppMain.class).web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        root.setId("Root");

        ConfigurableApplicationContext webApp = new SpringApplicationBuilder()
                .parent(root).sources(Api.class, TestAppMain.class).web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        webApp.setId("API");
    }
}
