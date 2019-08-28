package it.at7.gemini.gui.api;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.gui.ComponentException;
import it.at7.gemini.gui.annotation.GeminiGuiComponentHook;
import it.at7.gemini.gui.core.GuiComponentsManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping("/_components/{component}/{event}")
public class GuiComponentsController {

    private GuiComponentsManager guiComponentsManager;

    public GuiComponentsController(GuiComponentsManager guiComponentsManager) {
        this.guiComponentsManager = guiComponentsManager;
    }

    @RequestMapping(value = "/**")
    @ResponseStatus(HttpStatus.OK)
    Object allRequestHandler(@PathVariable String component,
                             @PathVariable String event,
                             @RequestBody(required = false) Object body,
                             HttpServletRequest request,
                             HttpServletResponse response) throws GeminiException {
        Optional<GeminiGuiComponentHook> optHook = guiComponentsManager.getHook(component);
        if (optHook.isPresent()) {
            GeminiGuiComponentHook hook = optHook.get();
            switch (EventMapping.valueOf(event.toUpperCase())) {
                case ONINIT:
                    return hook.onInit();
            }
            throw ComponentException.EVENT_NOT_FOUND(component, event);
        }

        throw ComponentException.COMPONENT_NOT_FOUND(component, event);
    }

    enum EventMapping {
        ONINIT
    }
}
