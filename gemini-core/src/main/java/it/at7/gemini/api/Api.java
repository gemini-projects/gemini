package it.at7.gemini.api;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.StateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@ComponentScan({"it.at7.gemini.api"})
@Service("API")
public class Api implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private StateManager stateManager;

    @PostConstruct
    public void init() {
        stateManager.changeState(State.API_INITIALIZATION);
    }

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        stateManager.changeState(State.API_INITIALIZED);
    }
}
