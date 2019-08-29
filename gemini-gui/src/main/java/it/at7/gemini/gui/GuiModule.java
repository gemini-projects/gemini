package it.at7.gemini.gui;

import it.at7.gemini.auth.core.AuthModuleRef;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.Module;
import it.at7.gemini.core.*;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static it.at7.gemini.conf.State.SCHEMA_STORAGE_INITIALIZED;

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
