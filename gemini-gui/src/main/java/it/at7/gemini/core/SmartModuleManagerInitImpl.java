package it.at7.gemini.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.at7.gemini.core.persistence.PersistenceEntityFilterUtilityService;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiGenericException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.SmartModuleEntity;
import it.at7.gemini.schema.SmartModuleEntityRef;
import it.at7.gemini.schema.SmartSchemaWrapper;
import it.at7.gemini.schema.smart.SmartSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
public class SmartModuleManagerInitImpl implements SmartModuleManagerInit {
    private static final Logger logger = LoggerFactory.getLogger(SmartModuleManagerInit.class);


    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;
    private final SchemaManagerInit schemaManagerInit;
    private final SchemaManager schemaManager;
    private final PersistenceEntityFilterUtilityService persistenceEntityFilterUtilityService;

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Autowired
    public SmartModuleManagerInitImpl(ApplicationContext applicationContext,
                                      EntityManager entityManager,
                                      SchemaManagerInit schemaManagerInit,
                                      SchemaManager schemaManager,
                                      PersistenceEntityFilterUtilityService persistenceEntityFilterUtilityService) {
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
        this.schemaManagerInit = schemaManagerInit;
        this.schemaManager = schemaManager;
        this.persistenceEntityFilterUtilityService = persistenceEntityFilterUtilityService;
    }

    @Override
    public SmartSchema parseSmartModule(String text) throws IOException {
        return yamlMapper.readValue(text, SmartSchema.class);
    }

    @Override
    public void initializeSmartModulesRecords(LinkedHashMap<SmartModule, SmartSchemaWrapper> schemasByModule, Transaction transaction) throws GeminiException {
        EntityOperationContext operationContext = schemaManagerInit.getOperationContextForInitSchema();
        for (Map.Entry<SmartModule, SmartSchemaWrapper> entry : schemasByModule.entrySet()) {
            SmartModule module = entry.getKey();
            SmartSchemaWrapper schemaWrapper = entry.getValue();

            if (!module.isDynamic()) {
                // static module have been handled by Gemini previous states
                SmartModuleEntity mEntity = SmartModuleEntity.of(module, schemaWrapper.getSmartSchema(), schemaWrapper.getSmartSchemaAsString());
                EntityRecord er = mEntity.toEntityRecord();
                this.entityManager.putOrUpdate(er, operationContext, transaction);
            } else {
                // dynamic module need to be checked and initialized once Gemini is UP
                // so let's check if we are in dynamic module initialization (creating the default schema)
                SmartSchema schema;
                try {
                    EntityRecord dynamicSmartModule = this.entityManager.get(SmartModuleEntityRef.NAME, module.getName().toUpperCase(), transaction);
                    String schemaString = dynamicSmartModule.get(SmartModuleEntityRef.FIELDS.SCHEMA);
                    try {
                        schema = yamlMapper.readValue(schemaString, SmartSchema.class);
                    } catch (IOException e) {
                        throw GeminiGenericException.wrap(e);
                    }
                    // create schema
                } catch (EntityRecordException.LkNotFoundException e) {
                    SmartModuleEntity mEntity = SmartModuleEntity.of(module, schemaWrapper.getSmartSchema(), schemaWrapper.getSmartSchemaAsString());
                    EntityRecord er = mEntity.toEntityRecord();
                    this.entityManager.putIfAbsent(er, operationContext, transaction);
                    schema = schemaWrapper.getSmartSchema();
                }
                schemaManagerInit.initDynamicSchema(module, schema.getRawSchema(module), operationContext, transaction);
            }
        }
        if (!schemasByModule.isEmpty()) {
            Entity smartModuleEntity = entityManager.getEntity(SmartModuleEntityRef.NAME);
            List<String> smartNames = schemasByModule.keySet().stream().map(SmartModule::getName).map(String::toUpperCase).collect(Collectors.toList());
            String condition = persistenceEntityFilterUtilityService.notContainsCondition("name", "smart_names");
            Map<String, Object> params = Map.of("smart_names", smartNames);
            List<EntityRecord> records = this.entityManager.getRecordsMatching(smartModuleEntity, FilterContext.withPersistenceQueryParam(condition, params), operationContext, transaction);
            for (EntityRecord record : records) {
                entityManager.delete(record, operationContext, transaction);
            }

            for (Map.Entry<SmartModule, SmartSchemaWrapper> moduleSchemaPair : schemasByModule.entrySet()) {
                handleGUIEntityRecords(moduleSchemaPair, transaction);
            }
        }
    }

    @Override
    public LinkedHashMap<SmartModule, SmartSchemaWrapper> loadSmartResources(List<SmartModule> modulesInOrder, Transaction transaction) throws GeminiException {
        LinkedHashMap<SmartModule, SmartSchemaWrapper> smartSchemas = new LinkedHashMap<>();
        try {
            for (SmartModule module : modulesInOrder) {
                if (!module.isDynamic()) {
                    String location = module.getSchemaResourceLocation();
                    Resource resource = applicationContext.getResource(location);
                    if (resource.exists()) {
                        logger.info("Smart Schema definition found for Smart Module {}: location {}", module.getName(), location);

                        smartSchemas.put(module, loadSmartSchemaFromResource(yamlMapper, resource));

                    } else {
                        logger.info("NO Smart Schema definition found for Smart Module {}: location {}", module.getName(), location);
                    }
                } else {
                    Resource resource = applicationContext.getResource(module.getDEFAULTSchemaResourceLocation());
                    assert resource.exists();
                    smartSchemas.put(module, loadSmartSchemaFromResource(yamlMapper, resource));
                }
            }
            return smartSchemas;
        } catch (Exception e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    private SmartSchemaWrapper loadSmartSchemaFromResource(ObjectMapper yamlMapper, Resource resource) throws IOException {
        InputStream schemaStream = resource.getInputStream();
        SmartSchema smartSchema = yamlMapper.readValue(schemaStream, SmartSchema.class);
        Scanner scanner = new Scanner(resource.getInputStream()).useDelimiter("\\A");
        String smartSchemaString = scanner.hasNext() ? scanner.next() : "";
        return SmartSchemaWrapper.of(smartSchema, smartSchemaString);
    }

    private void handleGUIEntityRecords(Map.Entry<SmartModule, SmartSchemaWrapper> moduleSchemaEntry, Transaction transaction) throws GeminiException {
        SmartModule smartModule = moduleSchemaEntry.getKey();
        SmartSchema smartSchema = moduleSchemaEntry.getValue().getSmartSchema();
        addALLRecords(smartSchema.getEntityGUIRecords(smartModule), transaction);
        addALLRecords(smartSchema.getFieldGUIRecords(smartModule), transaction);
    }

    private void addALLRecords(List<EntityRecord> records, Transaction transaction) throws GeminiException {
        for (EntityRecord entityGUIRecord : records) {
            this.entityManager.putOrUpdate(entityGUIRecord, this.schemaManagerInit.getOperationContextForInitSchema(), transaction);
        }
    }
}
