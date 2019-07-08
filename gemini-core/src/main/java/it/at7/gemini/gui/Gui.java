package it.at7.gemini.gui;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.StateManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@DependsOn("API")
@ComponentScan({"it.at7.gemini.gui"})
@Service("GUI")
public class Gui implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private StateManager stateManager;

    /**
     * This events is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            stateManager.changeState(State.WEB_APP_INITIALIZED);
        } catch (GeminiException e) {
            throw new GeminiRuntimeException(e);
        }
    }
}
