package it.at7.gemini.gui.api;


import it.at7.gemini.core.*;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.SmartModuleEntity;
import it.at7.gemini.schema.smart.SmartSchema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
public class DynamicSmartModule {

    public static final String PATH = "/_smartmodule/{module_name}";
    private final SmartModuleManagerInit smartModuleManagerInit;
    private final SchemaManager schemaManager;
    private final TransactionManager transactionManager;
    private final EntityManager entityManager;

    public DynamicSmartModule(SmartModuleManagerInit smartModuleManagerInit,
                              SchemaManager schemaManager,
                              TransactionManager transactionManager,
                              EntityManager entityManager) {
        this.smartModuleManagerInit = smartModuleManagerInit;
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
    }


    @PutMapping(value = PATH, consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String updateSchema(@PathVariable("module_name") String moduleName,
                               @RequestBody String yamlString) throws GeminiException {
        try {
            SmartSchema smartSchema = this.smartModuleManagerInit.parseSmartModule(yamlString);
            Optional<ModuleBase> module = schemaManager.getModule(moduleName);

            ModuleBase moduleBase = module.get();
            SmartModule smartModule = (SmartModule) moduleBase;
            RawSchema rawSchema = smartSchema.getRawSchema(smartModule);
            transactionManager.executeInSingleTrasaction(t ->
            {
                this.schemaManager.updateDynamicSchema(moduleBase, rawSchema, t);
                SmartModuleEntity mEntity = SmartModuleEntity.of(smartModule, smartSchema, yamlString);
                EntityRecord entityRecord = mEntity.toEntityRecord();
                this.entityManager.update(entityRecord, new EntityOperationContext(), t);
            });
        } catch (IOException e) {
            return "RCAMADONNA";
        }
        return "OK";
    }
}
