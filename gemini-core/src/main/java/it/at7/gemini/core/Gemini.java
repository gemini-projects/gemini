package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@ComponentScan({"it.at7.gemini.core"})
@Service
public class Gemini {
    private final static Logger logger = LoggerFactory.getLogger(Gemini.class);

    private final StateManagerImpl stateManager;
    private final SchemaManager schemaManager;
    private final ApplicationContext context;

    private Collection<Module> modules;
    private Map<String, Module> modulesInOrder;

    @Autowired
    public Gemini(StateManagerImpl stateManager, SchemaManager schemaManager, ApplicationContext applicationContext) {
        this.stateManager = stateManager;
        this.schemaManager = schemaManager;
        this.context = applicationContext;
    }

    public void init() {
        try {
            loadPredefinedBeans();
            loadModules();
            start();
            loadSchemas();
            initialize();
        } catch (Exception e) {
            logger.error("Error During start of Gemini", e);
            System.exit(1);
        }
    }

    private void loadPredefinedBeans() {
        context.getBean(Services.class);
    }

    private void loadModules() {
        logger.info("Loading Modules");
        Map<String, Module> modulesMap = context.getBeansOfType(Module.class);
        for (Module module : modulesMap.values()) {
            logger.info("Found module {} with dependecies {}", module.getName(), module.getDependencies());
            stateManager.register(module);
        }
        this.modulesInOrder = modulesMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getValue().getName().toUpperCase(), Map.Entry::getValue));
        // TODO topologically order modules
    }

    private void start() {
        stateManager.changeState(State.STARTED);
    }


    private void loadSchemas() throws Exception {
        logger.info("Initializing Schemas");
        schemaManager.initializeSchemas(modulesInOrder);
        stateManager.changeState(State.SCHEMA_INITIALIZED);
    }

    private void initialize() {
        stateManager.changeState(State.INITIALIZED);
    }

}
