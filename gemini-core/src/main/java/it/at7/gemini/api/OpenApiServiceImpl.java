package it.at7.gemini.api;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.Module;
import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenApiServiceImpl implements OpenApiService, StateListener {

    private final GeminiConfigurationService configurationService;
    private final StateManager stateManager;
    private final SchemaManager schemaManager;

    private OpenAPIBuilder openAPIBuilder;

    @Autowired
    public OpenApiServiceImpl(GeminiConfigurationService configurationService, StateManager stateManager, SchemaManager schemaManager) {
        this.configurationService = configurationService;
        this.stateManager = stateManager;
        this.schemaManager = schemaManager;
    }

    public void init() {
        if (configurationService.isOpenapiSchema()) {
            stateManager.register(this);
            openAPIBuilder = new OpenAPIBuilder();
        }
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        switch (actual) {
            case API_INITIALIZATION:
                makeOpenAPISchema();
                break;
            case API_INITIALIZED:
                storeOpenAPISchema();
                break;
            default:
                break;
        }
    }

    private void makeOpenAPISchema() {
        Collection<Entity> allEntities = this.schemaManager.getAllEntities();
        Map<Module, List<Entity>> entitiesByModule = allEntities.stream().collect(Collectors.groupingBy(Entity::getModule));
        List<Module> orderedModules = entitiesByModule.keySet().stream().sorted(Comparator.comparingInt(Module::order)).collect(Collectors.toList());
        openAPIBuilder.addModulesToTags(orderedModules);
        for (Module module : orderedModules) {
            List<Entity> entities = entitiesByModule.get(module);
            entities.forEach(e -> openAPIBuilder.addEntityPaths(e));
        }

    }

    private void storeOpenAPISchema() {
        String json = this.openAPIBuilder.toJsonString();
        try {
            File file = new File("./api/openapi.json");
            file.getParentFile().mkdirs();
            Files.write(Paths.get("./api/openapi.json"), json.getBytes());
        } catch (IOException e) {
            throw new GeminiRuntimeException(e);
        }
    }
}
