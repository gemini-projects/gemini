package it.at7.gemini.api.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.Module;
import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenApiServiceImpl implements OpenApiService, StateListener {
    private static final String SERVICE_INFO_DEFAULT_RESOURCE = "classpath:/service/default-info.yml";

    private final GeminiConfigurationService configurationService;
    private final StateManager stateManager;
    private final SchemaManager schemaManager;
    private final Environment environment;
    private final ApplicationContext context;

    private OpenAPIBuilder openAPIAllBuilder;
    private OpenAPIBuilder runtimeAPIBuilder;

    private List<Runnable> API_INITIALIZER_LISTENER = new ArrayList<>();

    @Autowired
    public OpenApiServiceImpl(GeminiConfigurationService configurationService, StateManager stateManager,
                              SchemaManager schemaManager, Environment environment,
                              ApplicationContext context
    ) {
        this.configurationService = configurationService;
        this.stateManager = stateManager;
        this.schemaManager = schemaManager;
        this.environment = environment;
        this.context = context;
        if (configurationService.isOpenapiSchema()) {
            this.stateManager.register(this);
            openAPIAllBuilder = new OpenAPIBuilder();
            runtimeAPIBuilder = new OpenAPIBuilder(false);
        }
    }

    private void createDefaultServiceInfoResource(File serviceInfoResource) throws IOException {
        Resource defaultInfoService = context.getResource(SERVICE_INFO_DEFAULT_RESOURCE);
        serviceInfoResource.getParentFile().mkdirs();
        Files.copy(defaultInfoService.getInputStream(), serviceInfoResource.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (configurationService.isOpenapiSchema()) {
            switch (actual) {
                case API_INITIALIZATION:
                    loadServiceInfo();
                    makeOpenAPISchema();
                    break;
                case API_INITIALIZED:
                    API_INITIALIZER_LISTENER.forEach(Runnable::run);
                    String localPort = environment.getProperty("local.server.port");
                    openAPIAllBuilder.addServer("http://127.0.0.1:" + localPort + "/api", "Local Server");
                    runtimeAPIBuilder.addServer("http://127.0.0.1:" + localPort + "/api", "Local Server");
                    storeOpenAPISchema(openAPIAllBuilder, "all.json");
                    storeOpenAPISchema(runtimeAPIBuilder, "runtime.json");
                    break;
                default:
                    break;
            }
        }
    }

    private void loadServiceInfo() {
        try {
            File serviceInfoFile = new File(configurationService.getServiceInfoResource());
            if (!serviceInfoFile.exists()) {
                createDefaultServiceInfoResource(serviceInfoFile);
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            GeminiServiceInfoWrapper geminiService = mapper.readValue(serviceInfoFile, GeminiServiceInfoWrapper.class);
            openAPIAllBuilder.addInfo(geminiService.info);
            runtimeAPIBuilder.addInfo(geminiService.info);
        } catch (IOException e) {
            throw new GeminiRuntimeException(e);
        }
    }

    private void makeOpenAPISchema() {
        Collection<Entity> allEntities = this.schemaManager.getAllEntities();
        Map<Module, List<Entity>> entitiesByModule = allEntities.stream().collect(Collectors.groupingBy(Entity::getModule));
        List<Module> orderedModules = entitiesByModule.keySet().stream().sorted(Comparator.comparingInt(Module::order)).collect(Collectors.toList());
        openAPIAllBuilder.addModulesToTags(orderedModules);
        for (Module module : orderedModules) {
            List<Entity> entities = entitiesByModule.get(module);
            entities.forEach(e -> openAPIAllBuilder.handleEntity(e));

            if (module.getName().equals("RUNTIME")) {
                entities.forEach(e -> runtimeAPIBuilder.handleEntity(e));
            } else {
                entities.forEach(e -> runtimeAPIBuilder.addComponentSchema(e, OpenAPIBuilder.SchemaType.ENTITY_LK));
            }
        }

    }

    private void storeOpenAPISchema(OpenAPIBuilder openAPIBuilder, String fileName) {
        String json = openAPIBuilder.toJsonString();
        try {
            String openApiDir = configurationService.getOpenApiDir();
            Path allPath = Paths.get(openApiDir, fileName);
            File file = new File(allPath.toString());
            file.getParentFile().mkdirs();
            Files.write(allPath, json.getBytes());
        } catch (IOException e) {
            throw new GeminiRuntimeException(e);
        }
    }

    @Override
    public void addOAuth2PasswordFlow(String name, Map<String, Object> flowParameters) {
        if (configurationService.isOpenapiSchema()) {
            String tokenUrl = (String) flowParameters.get("tokenUrl");
            if (tokenUrl == null || tokenUrl.equals("")) {
                throw new GeminiRuntimeException("Unable to initialize OAuth2 Password Flow - No tokenUrl Provided");
            }
            OpenAPIBuilder.SecuritySchema securitySchema = new OpenAPIBuilder.SecuritySchema();
            securitySchema.type = "oauth2";
            securitySchema.description = (String) flowParameters.getOrDefault("description", "See https://developers.getbase.com/docs/rest/articles/oauth2/requests");
            securitySchema.flows = Map.of("password",
                    Map.of("tokenUrl", tokenUrl,
                            "refreshUrl", flowParameters.get("refreshUrl"))
            );
            this.openAPIAllBuilder.addSecurityComponent(name, securitySchema);
            this.runtimeAPIBuilder.addSecurityComponent(name, securitySchema);
        }
    }

    @Override
    public void secureAllEntities(String securitySchemaName) {
        if (configurationService.isOpenapiSchema()) {
            API_INITIALIZER_LISTENER.add(() -> {
                this.openAPIAllBuilder.secureAllEntityPaths(securitySchemaName);
                this.runtimeAPIBuilder.secureAllEntityPaths(securitySchemaName);
            });
        }
    }

    /* @Override
    public void addPath(OpenAPIBuilder.Path path) {
        if (configurationService.isOpenapiSchema()) {
            API_INITIALIZER_LISTENER.add(() -> {
                this.openAPIAllBuilder.addPath(path);
            });
        }
    } */

    static class GeminiServiceInfoWrapper {
        public OpenAPIBuilder.Info info;
    }
}
