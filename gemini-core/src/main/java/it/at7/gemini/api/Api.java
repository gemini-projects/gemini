package it.at7.gemini.api;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.StateManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan({"it.at7.gemini.api"})
@Service("API")
public class Api implements ApplicationListener<ApplicationReadyEvent> {

    private StateManager stateManager;

    @Autowired
    public Api(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    /**
     * This events is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            stateManager.changeState(State.API_INITIALIZATION);
            stateManager.changeState(State.API_INITIALIZED);
        } catch (GeminiException e) {
            throw new GeminiRuntimeException(e);
        }
    }


    /**
     * Configuration to enable Cors when Spring Security is not used
     */
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**");
        }
    }
}
