package it.at7.gemini.gui;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.*;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.SmartSchemaWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static it.at7.gemini.conf.State.FRAMEWORK_SCHEMA_RECORDS_INITIALIZED;
import static it.at7.gemini.conf.State.GEMINI_MODULES_LOADED;

@Service
@ModuleDescription(
        name = "GUI",
        dependencies = {"AUTH"},
        order = -507)
@ComponentScan("it.at7.gemini.gui.components")
@ComponentScan("it.at7.gemini.gui.core")
@ComponentScan("it.at7.gemini.gui.events")
@ConditionalOnProperty(name = "gemini.gui", matchIfMissing = false)
public class GuiModule implements GeminiModule {
    private static final Logger logger = LoggerFactory.getLogger(GuiModule.class);

    private final ApplicationContext context;
    private final SmartModuleManagerInit smartModuleManagerInit;
    private final SchemaManagerInit schemaManagerInit;
    private List<SmartModule> smartModules;
    private LinkedHashMap<SmartModule, SmartSchemaWrapper> smartSchemas;


    @Autowired
    public GuiModule(ApplicationContext applicationContext,
                     SmartModuleManagerInit smartModuleManagerInit,
                     SchemaManagerInit schemaManagerInit) {
        this.context = applicationContext;
        this.smartModuleManagerInit = smartModuleManagerInit;
        this.schemaManagerInit = schemaManagerInit;
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (actual.equals(GEMINI_MODULES_LOADED)) {
            smartSchemas = loadSmartSchemas();
            addSmartSchemasToGemini();
        }

        if (actual.equals(FRAMEWORK_SCHEMA_RECORDS_INITIALIZED)) {
            assert transaction.isPresent();
            smartModuleManagerInit.initializeSmartModulesRecords(smartSchemas, transaction.get());
        }
    }

    private LinkedHashMap<SmartModule, SmartSchemaWrapper> loadSmartSchemas() throws GeminiException {
        loadSmartModules();
        return smartModuleManagerInit.loadSmartResources(smartModules);
    }

    private void addSmartSchemasToGemini() {
        Map<ModuleBase, RawSchema> rawSchemas = smartSchemas.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, s -> s.getValue().getSmartSchema().getRawSchema(s.getKey())));
        this.schemaManagerInit.addExternalSchemas(rawSchemas);
    }

    private void loadSmartModules() {
        logger.info("SMART MODULES LOADING");
        Map<String, SmartModule> modulesMap = context.getBeansOfType(SmartModule.class);
        this.smartModules = modulesMap.values().stream()
                .sorted(Comparator.comparingInt(SmartModule::order))
                .collect(Collectors.toList());
        for (SmartModule module : smartModules) {
            logger.info("Found SMART Module {} with dependecies {}", module.getName(), module.getDependencies());
        }
    }
}
