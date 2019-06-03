package it.at7.gemini.api.openapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.core.Module;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;

import java.util.*;

public class OpenAPIBuilder {
    public static String OPENAPI_VERSION = "3.0.2";
    public static String INFO_TITLE = "Gemini";

    private final List<Tag> tags = new ArrayList<>();
    private final List<Server> servers = new ArrayList<>();
    private final Map<String, Path> pathsByName = new LinkedHashMap<>();
    private final Map<String, Object> components = new LinkedHashMap<>();

    private final Map<String, String> UNAUTHORIZED_REF = Map.of("$ref", "#/components/responses/Unauthorized");
    private final Map<String, String> DEFAULTERR_REF = Map.of("$ref", "#/components/responses/DefaultError");

    public OpenAPIBuilder() {
        initComponents();
    }

    private void initComponents() {
        addServer("/", "Root Server");
        components.put("responses", initResponseComponent());
        components.put("schemas", initSchemasComponent());
    }

    private Object initSchemasComponent() {
        return new LinkedHashMap<String, Schema>();
    }

    private Map<String, Response> initResponseComponent() {
        Map<String, Response> responses = new LinkedHashMap<>();
        responses.put("Unauthorized", makeUnauthorizedResponse());
        responses.put("DefaultError", makeDefaultResponse());
        return responses;
    }

    private Response makeUnauthorizedResponse() {
        Response response = new Response();
        response.description = "Unauthorized";
        return response;
    }

    private Response makeDefaultResponse() {
        Response response = new Response();
        response.description = "Unexpected error";
        return response;
    }

    public String toJsonString() {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Object> rootJson = new LinkedHashMap<>();
        rootJson.put("openapi", OPENAPI_VERSION);
        rootJson.put("info", makeInfo());
        rootJson.put("servers", servers);
        rootJson.put("paths", pathsByName);
        rootJson.put("tags", tags);
        rootJson.put("components", components);
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(rootJson);
        } catch (JsonProcessingException e) {
            throw new GeminiRuntimeException("Unable to serialize JSON");
        }
    }

    private Map<String, Object> makeInfo() {
        HashMap<String, Object> infoJson = new HashMap<>();
        infoJson.put("title", INFO_TITLE);
        infoJson.put("version", "1");
        return infoJson;
    }

    public void addModulesToTags(List<Module> orderedModules) {
        for (Module module : orderedModules) {
            tags.add(Tag.from(module.getName(), String.format("Module %s", module.getName())));
        }
    }

    public void addEntityPaths(Entity entity) {

        String entityName = entity.getName().toLowerCase();
        Path rootEntityPath = new Path();
        rootEntityPath.summary = String.format("%s resource route", entity.getName());

        // Embedable entities are not exposed
        if(!entity.isEmbedable()) {
            // GET and POST on /entityname
            rootEntityPath.get = getEntityListMethod(entity);
            rootEntityPath.post = postNewEntityMethod(entity);
            this.pathsByName.put("/" + entityName, rootEntityPath);
        }

        // SCHEMAS
        addComponentSchema(entity);
    }

    private Method getEntityListMethod(Entity entity) {
        Method method = new Method();
        method.summary = String.format("Get the list from %s resources", entity.getName());
        method.tags = List.of(entity.getModule().getName());

        // 200 response has not components default
        Response response200 = new Response();
        response200.description = "Successful operation";
        // todo reponse json
        method.responses.put("200", response200);

        method.responses.put("401", UNAUTHORIZED_REF);
        method.responses.put("default", DEFAULTERR_REF);

        return method;
    }

    private Method postNewEntityMethod(Entity entity) {
        Method method = new Method();
        method.summary = String.format("Create a new %s resource", entity.getName());
        method.tags = List.of(entity.getModule().getName());

        method.requestBody = new RequestBody();
        method.requestBody.required = true;
        method.requestBody.content.put("application/json",
                Map.of("schema",
                        Map.of("$ref", String.format("#/components/schemas/%s", entity.getName()))));

        // 200 response has not components default
        Response response200 = new Response();
        response200.description = "Resource created";
        // todo reponse json
        method.responses.put("200", response200);

        method.responses.put("401", UNAUTHORIZED_REF);
        method.responses.put("default", DEFAULTERR_REF);
        return method;
    }

    public void addServer(String url, String description) {
        this.servers.add(Server.from(url, description));
    }


    private void addComponentSchema(Entity entity) {
        Schema schema = new Schema();
        schema.type = "object";
        schema.properties = new LinkedHashMap<>();
        for (EntityField field : entity.getDataEntityFields()) {
            String name = field.getName().toLowerCase();
            SchemaProperty schemaProperty = new SchemaProperty();
            switch (field.getType()) {
                case TEXT:
                    schemaProperty.type = "string";
                    break;
                case NUMBER:
                    schemaProperty.type = "number";
                    break;
                case LONG:
                    schemaProperty.type = "integer";
                    schemaProperty.format = "int64";
                    break;
                case DOUBLE:
                    schemaProperty.type = "number";
                    schemaProperty.format = "double";
                    break;
                case BOOL:
                    schemaProperty.type = "boolean";
                    break;
                case TIME:
                    schemaProperty.type = "string";
                    schemaProperty.format = "time";
                    break;
                case DATE:
                    schemaProperty.type = "string";
                    schemaProperty.format = "date";
                    break;
                case DATETIME:
                    schemaProperty.type = "string";
                    schemaProperty.format = "date-time";
                    break;
                case PASSWORD:
                    schemaProperty.type = "string";
                    schemaProperty.format = "password";
                    break;
                case ENTITY_REF:
                    schemaProperty.type = "object";
                    schemaProperty.$ref = String.format("#/components/schemas/%s", field.getEntityRef().getName());
                    break;
                case ENTITY_EMBEDED:
                    schemaProperty.type = "object";
                    break;
                case GENERIC_ENTITY_REF:
                    break;
                case TEXT_ARRAY:
                    schemaProperty.type = "array";
                    schemaProperty.items = new SchemaProperty();
                    schemaProperty.items.type = "string";
                    break;
                case ENTITY_REF_ARRAY:
                    schemaProperty.type = "array";
                    schemaProperty.items = new SchemaProperty();
                    schemaProperty.items.type = "object";
                    break;
                case RECORD:
                    break;
            }


            schema.properties.put(name, schemaProperty);
        }
        Map<String, Schema> schemas = (Map<String, Schema>) this.components.get("schemas");
        schemas.put(entity.getName(), schema);
    }


    static class Tag {

        public String name;
        public String description;

        private Tag(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static Tag from(String name, String description) {
            return new Tag(name, description);
        }
    }

    static class Path {
        public String summary;
        public String description;
        public Method get;
        public Method put;
        public Method post;
    }

    static class Method {
        public String summary;
        public List<String> tags;
        public RequestBody requestBody;
        public Map<String, Object> responses = new LinkedHashMap<>();
    }

    static class RequestBody {
        public String description;
        public boolean required;
        public Map<String, Object> content = new LinkedHashMap<>();
    }

    static class Response {
        public String description;
    }

    static class Server {
        public String url;
        public String description;

        private Server(String url, String description) {
            this.url = url;
            this.description = description;
        }

        public static Server from(String url, String description) {
            return new Server(url, description);
        }
    }

    static class Schema {
        public String type;
        public Map<String, SchemaProperty> properties;
    }

    static class SchemaProperty {
        public String type;
        public String format;
        public SchemaProperty items;
        public String $ref;
    }
}
