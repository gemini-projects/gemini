package it.at7.gemini.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiGenericException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.SmartModuleEntity;
import it.at7.gemini.schema.SmartModuleEntityRef;
import it.at7.gemini.schema.smart.SmartSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SmartModuleManagerInitImpl implements SmartModuleManagerInit {
    private static final Logger logger = LoggerFactory.getLogger(SmartModuleManagerInit.class);


    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;
    private final SchemaManagerInit schemaManagerInit;

    @Autowired
    public SmartModuleManagerInitImpl(ApplicationContext applicationContext, EntityManager entityManager, SchemaManagerInit schemaManagerInit) {
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
        this.schemaManagerInit = schemaManagerInit;
    }

    @Override
    public void initializeSmartModulesRecords(LinkedHashMap<SmartModule, SmartSchema> schemasByModule, Transaction transaction) throws GeminiException {
        EntityOperationContext operationContext = schemaManagerInit.getOperationContextForInitSchema();
        for (SmartModule smartModule : schemasByModule.keySet()) {
            SmartModuleEntity mEntity = SmartModuleEntity.of(smartModule);
            EntityRecord er = mEntity.toEntityRecord();
            this.entityManager.putOrUpdate(er, operationContext, transaction);
        }
        Entity smartModuleEntity = entityManager.getEntity(SmartModuleEntityRef.NAME);
        List<String> smarNames = schemasByModule.keySet().stream().map(SmartModule::getName).map(String::toUpperCase).collect(Collectors.toList());
        // TODO persistence interface to have a not IN filter query
        String condition = " name NOT IN (:smart_names)";
        Map<String, Object> params = Map.of("smart_names", smarNames);
        List<EntityRecord> records = this.entityManager.getRecordsMatching(smartModuleEntity, FilterContext.withPersistenceQueryParam(condition, params), operationContext, transaction);
        for (EntityRecord record : records) {
            entityManager.delete(record, operationContext, transaction);
        }

        for (Map.Entry<SmartModule, SmartSchema> moduleSchemaPair : schemasByModule.entrySet()) {
            handleGUIEntityRecords(moduleSchemaPair, transaction);
        }
    }

    @Override
    public LinkedHashMap<SmartModule, SmartSchema> loadSmartResources(List<SmartModule> modulesInOrder) throws GeminiException {
        LinkedHashMap<SmartModule, SmartSchema> smartSchema = new LinkedHashMap<>();
        try {
            for (SmartModule module : modulesInOrder) {
                if (!module.isDynamic()) {
                    String location = module.getSchemaResourceLocation();
                    Resource resource = applicationContext.getResource(location);
                    if (resource.exists()) {
                        InputStream schemaStream = resource.getInputStream();
                        logger.info("Smart Schema definition found for Smart Module {}: location {}", module.getName(), location);

                        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
                        smartSchema.put(module, yamlMapper.readValue(schemaStream, SmartSchema.class));
                    } else {
                        logger.info("NO Smart Schema definition found for Smart Module {}: location {}", module.getName(), location);
                    }
                }
            }
            return smartSchema;
        } catch (Exception e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    private void handleGUIEntityRecords(Map.Entry<SmartModule, SmartSchema> moduleSchemaEntry, Transaction transaction) throws GeminiException {
        SmartModule smartModule = moduleSchemaEntry.getKey();
        SmartSchema smartSchema = moduleSchemaEntry.getValue();
        addALLRecords(smartSchema.getEntityGUIRecords(smartModule), transaction);
        addALLRecords(smartSchema.getFieldGUIRecords(smartModule), transaction);
    }

    private void addALLRecords(List<EntityRecord> records, Transaction transaction) throws GeminiException {
        for (EntityRecord entityGUIRecord : records) {
            this.entityManager.putOrUpdate(entityGUIRecord, this.schemaManagerInit.getOperationContextForInitSchema(), transaction);
        }
    }
}
