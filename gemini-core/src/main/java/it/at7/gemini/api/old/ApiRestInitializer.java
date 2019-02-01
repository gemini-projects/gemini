package it.at7.gemini.api.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

public class ApiRestInitializer implements WebApplicationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApiRestInitializer.class);

    private static ApplicationContext defaultAppContext;

    public static void setDefaultApplicationContext(ApplicationContext applicationContext){
        ApiRestInitializer.defaultAppContext = applicationContext;
    }

    @Override
    public void onStartup(ServletContext container) {
        logger.info("************************ WEBAPP");
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.setParent(defaultAppContext);
        dispatcherContext.scan("it.at7.gemini.api");
        dispatcherContext.refresh();


        // Register and map the dispatcher servlet
        ServletRegistration.Dynamic dispatcher = container
                .addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }
}