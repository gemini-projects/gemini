package it.at7.gemini.gui;

import it.at7.gemini.core.Module;
import it.at7.gemini.core.ModuleDescription;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "GUI",
        dependencies = {"AUTH"},
        order = -507)
@ComponentScan("it.at7.gemini.gui.components")
@ComponentScan("it.at7.gemini.gui.core")
@ComponentScan("it.at7.gemini.gui.events")
@ConditionalOnProperty(name = "gemini.gui", matchIfMissing = false)
public class GuiModule implements Module {
}
