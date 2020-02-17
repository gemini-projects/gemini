package it.at7.gemini.gui.api;


import it.at7.gemini.api.ApiError;
import it.at7.gemini.api.RestAPIControllerInterface;
import it.at7.gemini.core.*;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.SmartModuleEntity;
import it.at7.gemini.schema.smart.SmartSchema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static it.at7.gemini.core.EntityManagerImpl.DYNAMIC_SCHEMA_CONTEXT_FLAG;

@RestController
public class DynamicSmartModule {

    public static final String PATH = "/_smartmodule/{module_name}";
    private final SmartModuleManagerInit smartModuleManagerInit;
    private final SchemaManager schemaManager;
    private final TransactionManager transactionManager;
    private final EntityManager entityManager;
    private final RestAPIControllerInterface restAPIControllerInterface;

    public DynamicSmartModule(SmartModuleManagerInit smartModuleManagerInit,
                              SchemaManager schemaManager,
                              TransactionManager transactionManager,
                              EntityManager entityManager,
                              RestAPIControllerInterface restAPIControllerInterface) {
        this.smartModuleManagerInit = smartModuleManagerInit;
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
        this.restAPIControllerInterface = restAPIControllerInterface;
    }


    @PutMapping(value = PATH, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String updateSchema(@PathVariable("module_name") String moduleName,
                               @RequestBody String yamlString,
                               HttpServletRequest request) throws GeminiException, ApiError {
        try {
            SmartSchema smartSchema = this.smartModuleManagerInit.parseSmartModule(yamlString);
            Optional<ModuleBase> module = schemaManager.getModule(moduleName);

            ModuleBase moduleBase = module.get();
            SmartModule smartModule = (SmartModule) moduleBase;
            RawSchema rawSchema = smartSchema.getRawSchema(smartModule);
            transactionManager.executeEntityManagedTransaction(t ->
            {
                EntityOperationContext entityOperationContext = restAPIControllerInterface.createEntityOperationContext(request);
                entityOperationContext.putFlag(DYNAMIC_SCHEMA_CONTEXT_FLAG);
                this.schemaManager.updateDynamicSchema(moduleBase, rawSchema, entityOperationContext, t);
                SmartModuleEntity mEntity = SmartModuleEntity.of(smartModule, smartSchema, yamlString);
                EntityRecord entityRecord = mEntity.toEntityRecord();
                this.entityManager.update(entityRecord, entityOperationContext, t);
            });
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return "OK";
    }
}
