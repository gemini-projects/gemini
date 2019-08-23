package it.at7.gemini.gui;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.StateManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Service
@ComponentScan({"it.at7.gemini.gui.api"})
@ConditionalOnProperty(name = "gemini.gui", matchIfMissing = false)
public class GuiAPI implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private StateManager stateManager;

    /**
     * This events is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            stateManager.changeState(State.GUI_INITIALIZED);
        } catch (GeminiException e) {
            throw new GeminiRuntimeException(e);
        }
    }
}
