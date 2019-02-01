package it.at7.gemini.boot;

import it.at7.gemini.api.Api;
import it.at7.gemini.core.Gemini;
import it.at7.gemini.gui.Gui;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@EnableAutoConfiguration
public class AnnotationAppMain {
    public static void main(String[] args) {

        ConfigurableApplicationContext root = new SpringApplicationBuilder()
                .parent(Gemini.class, AnnotationAppMain.class).web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        root.setId("GEMINI-ROOT");
        Gemini gemini = root.getBean(Gemini.class);
        gemini.init();

        /* ConfigurableApplicationContext api = new SpringApplicationBuilder()
                .parent(root).sources(Api.class, AnnotationAppMain.class).web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        api.setId("GEMINI-API"); */

        ConfigurableApplicationContext gui = new SpringApplicationBuilder()
                .parent(root).sources(Api.class, Gui.class, AnnotationAppMain.class).web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
        gui.setId("GEMINI-GUI");


    }
}
