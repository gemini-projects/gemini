package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.events.EventManagerInit;
import it.at7.gemini.exceptions.GeminiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ComponentScan({"it.at7.gemini.core"})
@Service
public class Gemini {
    private final static Logger logger = LoggerFactory.getLogger(Gemini.class);

    private final ApplicationContext context;
    private final StateManagerImpl stateManager;
    private final SchemaManagerInit schemaManagerInit;
    private final EventManagerInit eventManagerInit;
    private final TransactionManager transactionManager;
    private List<GeminiModule> geminiModules;

    @Autowired
    public Gemini(StateManagerImpl stateManager, SchemaManagerInit schemaManagerInit,
                  ApplicationContext applicationContext, EventManagerInit eventManagerInit,
                  TransactionManager transactionManager) {
        this.stateManager = stateManager;
        this.schemaManagerInit = schemaManagerInit;
        this.context = applicationContext;
        this.eventManagerInit = eventManagerInit;
        this.transactionManager = transactionManager;
    }

    public void init() {
        try {
            loadCoreServices();
            loadGeminiModules();
            start();
            transactionManager.executeEntityManagedTransaction(this::initializeSchemaAndEvents);
            initialize();
        } catch (Exception e) {
            logger.error("Error During start of Gemini", e);
            System.exit(1);
        }
    }

    private void loadCoreServices() {
        context.getBean(Services.class);
    }

    private void loadGeminiModules() {
        logger.info("GEMINI MODULES LOADING");
        Map<String, GeminiModule> modulesMap = context.getBeansOfType(GeminiModule.class);
        this.geminiModules = modulesMap.values().stream()
                .sorted(Comparator.comparingInt(GeminiModule::order))
                .collect(Collectors.toList());
        for (GeminiModule module : geminiModules) {
            logger.info("Found Module {} with dependecies {}", module.getName(), module.getDependencies());
            stateManager.register(module);
        }
    }

    private void start() throws GeminiException {
        stateManager.changeState(State.STARTED, Optional.empty());
    }


    private void initializeSchemaAndEvents(Transaction transaction) throws GeminiException {
        logger.info("SCHEMAS/EVENTS INITIALIZATION");
        schemaManagerInit.loadGeminiModulesSchemas(geminiModules);
        stateManager.changeState(State.GEMINI_MODULES_LOADED, Optional.of(transaction));
        schemaManagerInit.initializeSchemaStorage(transaction);
        stateManager.changeState(State.SCHEMA_STORAGE_INITIALIZED, Optional.of(transaction));
        eventManagerInit.loadEvents();
        stateManager.changeState(State.SCHEMA_EVENTS_LOADED, Optional.of(transaction));
        schemaManagerInit.initializeSchemaEntityRecords(transaction);
    }

    private void initialize() throws GeminiException {
        stateManager.changeState(State.INITIALIZED, Optional.empty());
    }

}
