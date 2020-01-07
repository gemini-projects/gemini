package it.at7.gemini.gui;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.*;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.gui.schema.EntityGUIRef;
import it.at7.gemini.gui.schema.FieldGUIRef;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.SmartSchemaWrapper;
import it.at7.gemini.schema.smart.SmartSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static it.at7.gemini.conf.State.*;
import static it.at7.gemini.core.EntityManagerImpl.DYNAMIC_SCHEMA_CONTEXT_FLAG;

@Service
@ModuleDescription(
        name = "GUI",
        dependencies = {"AUTH"},
        order = -507)
@ComponentScan("it.at7.gemini.gui.components")
@ComponentScan("it.at7.gemini.gui.core")
@ComponentScan("it.at7.gemini.gui.events")
@ConditionalOnProperty(name = "gemini.gui", havingValue = "true")
public class GuiModule implements GeminiModule, SchemaManagerInitListener {
    private static final Logger logger = LoggerFactory.getLogger(GuiModule.class);

    private final ApplicationContext context;
    private final SmartModuleManagerInit smartModuleManagerInit;
    private final SchemaManagerInit schemaManagerInit;
    private final EntityManager entityManager;
    private List<SmartModule> smartModules;
    private LinkedHashMap<SmartModule, SmartSchemaWrapper> smartSchemas;


    @Autowired
    public GuiModule(ApplicationContext applicationContext,
                     SmartModuleManagerInit smartModuleManagerInit,
                     SchemaManagerInit schemaManagerInit,
                     EntityManager entityManager) {
        this.context = applicationContext;
        this.smartModuleManagerInit = smartModuleManagerInit;
        this.schemaManagerInit = schemaManagerInit;
        this.entityManager = entityManager;
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (actual.equals(GEMINI_MODULES_LOADED)) {
            assert transaction.isPresent();
            smartSchemas = loadSmartSchemas(transaction.get());
            addSmartSchemasToGemini();
        }

        if (actual.equals(SCHEMA_STORAGE_INITIALIZED)) {
            Entity guiEntity = this.entityManager.getEntity(EntityGUIRef.NAME);
            Entity guiField = this.entityManager.getEntity(FieldGUIRef.NAME);
            schemaManagerInit.addEntityFieldsToDelete(guiEntity.getField(EntityGUIRef.FIELDS.ENTITY));
            schemaManagerInit.addEntityFieldsToDelete(guiField.getField(FieldGUIRef.FIELDS.FIELD));
        }

        if (actual.equals(FRAMEWORK_SCHEMA_RECORDS_INITIALIZED)) {
            assert transaction.isPresent();
            smartModuleManagerInit.initializeSmartModulesRecords(smartSchemas, transaction.get());
        }
    }

    @Override
    public void onSchemasEntityRecords(EntityOperationContext entityOperationContext) {
        entityOperationContext.putFlag(DYNAMIC_SCHEMA_CONTEXT_FLAG);
    }

    private LinkedHashMap<SmartModule, SmartSchemaWrapper> loadSmartSchemas(Transaction transaction) throws GeminiException {
        loadSmartModules();
        return smartModuleManagerInit.loadSmartResources(smartModules, transaction);
    }

    private void addSmartSchemasToGemini() {
        Map<ModuleBase, RawSchema> rawStaticSchemas = new HashMap<>();
        for (Map.Entry<SmartModule, SmartSchemaWrapper> entry : smartSchemas.entrySet()) {
            SmartModule module = entry.getKey();
            if (!module.isDynamic()) {
                SmartSchemaWrapper wrapper = entry.getValue();
                SmartSchema smartSchema = wrapper.getSmartSchema();
                RawSchema optRawSchema = smartSchema.getRawSchema(module);
                rawStaticSchemas.put(module, optRawSchema);
            }
        }
        this.schemaManagerInit.addExternalSchemas(rawStaticSchemas);
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
