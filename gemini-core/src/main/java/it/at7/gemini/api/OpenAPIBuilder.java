package it.at7.gemini.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.core.Module;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;

import java.util.*;

public class OpenAPIBuilder {
    public static String OPENAPI_VERSION = "3.0.2";
    public static String INFO_TITLE = "Gemini";

    private final List<Tag> tags = new ArrayList<>();
    private final Map<String, Path> pathsByName = new LinkedHashMap<>();

    public String toJsonString() {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Object> rootJson = new HashMap<>();
        rootJson.put("openapi", OPENAPI_VERSION);
        rootJson.put("info", makeInfo());
        rootJson.put("paths", pathsByName);
        rootJson.put("tags", tags);
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
            tags.add(Tag.of(module.getName(), String.format("Module %s", module.getName())));
        }
    }

    public void addEntityPaths(Entity entity) {

        String entityName = entity.getName().toLowerCase();
        Path rootEntityPath = new Path();
        rootEntityPath.summary = String.format("%s resource route", entity.getName());
        rootEntityPath.get = getEntityListMethod(entity);
        this.pathsByName.put("/" + entityName, rootEntityPath);
    }

    private Method getEntityListMethod(Entity entity) {
        Method method = new Method();
        method.summary = String.format("Get the list of %s resources", entity.getName());
        method.tags = List.of(entity.getModule().getName());
        method.responses = new HashMap<>();
        Response response200 = new Response();
        response200.description = "Successful operation";
        // todo reponse json
        method.responses.put("200", response200);

        return method;
    }


    static class Tag {
        public String name;
        public String description;

        public Tag(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static Tag of(String name, String description) {
            return new Tag(name, description);
        }
    }

    static class Path {
        public String summary;
        public String description;
        public Method get;
        public Method put;
    }

    static class Method {
        public String summary;
        public List<String> tags;
        public Map<String, Response> responses;
    }

    static class Response {
        public String description;
    }
}
