package it.at7.gemini.api;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.EntityException;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.InvalidRequesException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static it.at7.gemini.api.ApiUtility.*;
import static it.at7.gemini.api.RestAPIController.API_URL;

@RestController
@RequestMapping(API_URL + "/{entity}")
public class RestAPIController {

    public static final String API_URL = "/api";

    private EntityManager entityManager;
    private GeminiConfigurationService configurationService;
    private ApiListenersManager apiListenersManager;

    @Autowired
    public RestAPIController(EntityManager entityManager, GeminiConfigurationService configurationService, ApiListenersManager apiListenersManager) {
        this.entityManager = entityManager;
        this.configurationService = configurationService;
        this.apiListenersManager = apiListenersManager;
    }

    @RequestMapping(value = "/**")
    @ResponseStatus(HttpStatus.OK)
    Object allRequestHandler(@PathVariable String entity,
                             @RequestBody(required = false) Object body,
                             HttpServletRequest request,
                             HttpServletResponse response) throws GeminiException {

        List<String> geminiHeaderValues = getGeminiHeader(request);
        Object results = requestHandler(entity, body, request, response);
        if (noGeminiDataType(geminiHeaderValues, request.getHeader("Accept"))) {
            return results;
        }
        return handleGeminiDataTypeResponse(results, request, response);
    }

    private Object handleGeminiDataTypeResponse(Object results, HttpServletRequest request, HttpServletResponse response) throws InvalidRequesException {
        response.setHeader(GEMINI_HEADER, GEMINI_API_META_TYPE);
        if (results instanceof EntityRecord) {
            return GeminiWrappers.EntityRecordApiType.of((EntityRecord) results);
        }
        if (results instanceof GeminiWrappers.EntityRecordsList) {
            return GeminiWrappers.EntityRecordListApiType.of((GeminiWrappers.EntityRecordsList) results);
        }
        throw InvalidRequesException.CANNOT_HANDLE_REQUEST();
    }

    private Object requestHandler(String entity, Object body, HttpServletRequest request, HttpServletResponse response) throws GeminiException {
        Entity e = checkEntity(entity.toUpperCase());
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(request.getRequestURI()).build();
        List<String> paths = uriComponents.getPathSegments();
        ensurePathsAreConsistent(paths, e);
        String method = request.getMethod();
        Map<String, String[]> parameters = request.getParameterMap(); // query string params
        List<String> geminiHeader = getGeminiHeader(request);
        EntityOperationContext entityOperationContext = createEntityOperationContext(entity, body, request);
        if (paths.size() == 2) {
            // this is a root entity requet - METHOD ALLOWED POST AND GET (for list)
            switch (method) {
                case "POST":
                    if (body == null) {
                        throw InvalidRequesException.BODY_REQUIRED();
                    }
                    return handleInsertRecord(e, geminiHeader, body, entityOperationContext);
                case "GET":
                    return handleGetEntityList(e, parameters, entityOperationContext);
                default:
                    throw InvalidRequesException.INVALID_METHOD_FOR_REQUEST(method);
            }
        }

        if (paths.size() > 2) {
            int requestLkLenght = paths.size() - 2;
            Entity.LogicalKey logicalKey = e.getLogicalKey();
            List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
            if (requestLkLenght != 1 && logicalKeyList.size() < requestLkLenght) {
                // it is not a UUID and it is a multi lk entity request
                throw InvalidRequesException.CANNOT_HANDLE_REQUEST();
            }
            if (requestLkLenght == 1 || logicalKeyList.size() == requestLkLenght) {
                // we can query a entity record so we can GET/UPDATE/DELETE
                String[] lkStringsArray = decodeLogicalKeyStrings(paths);
                switch (method) {
                    case "GET":
                        return handleGetRecord(e, entityOperationContext, lkStringsArray);
                    case "PUT":
                        return handleUpdateRecord(e, body, entityOperationContext, lkStringsArray);
                    case "DELETE":
                        return handleDeleteRecord(e, entityOperationContext, lkStringsArray);
                    default:
                        throw InvalidRequesException.INVALID_METHOD_FOR_REQUEST(method);
                }
            }
            if (logicalKeyList.size() > requestLkLenght) {
                // we are quering a subfield or a subfield list
                throw InvalidRequesException.CANNOT_HANDLE_REQUEST();
            }
        }
        throw InvalidRequesException.CANNOT_HANDLE_REQUEST();
    }

    private EntityOperationContext createEntityOperationContext(String entity, Object body, HttpServletRequest request) {
        EntityOperationContext entityOperationContext = new EntityOperationContext();
        for (RestAPIControllerListener apiControllerListener : apiListenersManager.getApiControllerListeners()) {
            apiControllerListener.onEntityOperationContextCreate(entity, body, request, entityOperationContext);
        }
        return entityOperationContext;
    }

    @NotNull
    private String[] decodeLogicalKeyStrings(List<String> paths) {
        List<String> lkStrings = paths.subList(2, paths.size());
        lkStrings = lkStrings.stream().map(s -> {
            try {
                return URLDecoder.decode(s, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e1) {
                throw new RuntimeException("Decode logical key failed");
            }
        }).collect(Collectors.toList());
        return lkStrings.toArray(new String[0]);
    }

    private List<String> getGeminiHeader(HttpServletRequest request) {
        String header = request.getHeader(GEMINI_HEADER);
        return header == null ? Collections.emptyList() : Arrays.asList(header.split(","));
    }


    private GeminiWrappers.EntityRecordsList handleGetEntityList(Entity e, Map<String, String[]> parameters, EntityOperationContext entityOperationContext) throws GeminiException {
        FilterContext filterContext = new FilterContextBuilder(configurationService)
                .fromParameters(parameters)
                .build();
        List<EntityRecord> recordList = entityManager.getRecordsMatching(e, filterContext, entityOperationContext);
        // TODO add entity Operation Context ??
        return GeminiWrappers.EntityRecordsList.of(recordList, filterContext);
    }


    private Object handleInsertRecord(Entity e, List<String> geminiHeader, Object body, EntityOperationContext entityOperationContext) throws GeminiException {
        if (geminiDataType(geminiHeader)) {
            if (Map.class.isAssignableFrom(body.getClass())) {
                Map<String, Object> mapBody = (Map<String, Object>) body;
                if (RecordConverters.containGeminiDataTypeFields(mapBody))
                    return handleInsertRecord(e, body, entityOperationContext);
            }
        }
        // even if the header is geminiDataType let's to handle insert without meta
        return handleInsertRecord(e, body, entityOperationContext);
    }

    private Object handleInsertRecord(Entity e, Object body, EntityOperationContext entityOperationContext) throws GeminiException {
        if (Map.class.isAssignableFrom(body.getClass()))
            return handleInsertRecord((Map<String, Object>) body, e, entityOperationContext);
        if (List.class.isAssignableFrom(body.getClass())) {
            return handleInsertRecords((List<Map<String, Object>>) body, e, entityOperationContext);
        }
        throw InvalidRequesException.INVALID_BODY();
    }

    private EntityRecord handleInsertRecord(Map<String, Object> body, Entity e, EntityOperationContext entityOperationContext) throws GeminiException {
        EntityRecord rec = RecordConverters.entityRecordFromMap(e, body);
        return entityManager.putIfAbsent(rec, entityOperationContext);
    }

    private Object handleInsertRecords(List<Map<String, Object>> body, Entity e, EntityOperationContext entityOperationContext) throws GeminiException {
        List<EntityRecord> records = new ArrayList<>();
        for (Map<String, Object> record : body) {
            records.add(RecordConverters.entityRecordFromMap(e, record));
        }
        Collection<EntityRecord> entityRecords = entityManager.putIfAbsent(records, entityOperationContext);
        return GeminiWrappers.EntityRecordsList.of(entityRecords);
    }


    private Object handleUpdateRecord(Entity e, Object body, EntityOperationContext entityOperationContext, String... logicalKey) throws GeminiException {
        if (Map.class.isAssignableFrom(body.getClass()))
            return handleUpdateRecord(e, (Map<String, Object>) body, entityOperationContext, logicalKey);
        throw InvalidRequesException.INVALID_BODY();
    }

    private EntityRecord handleUpdateRecord(Entity e, Map<String, Object> body, EntityOperationContext entityOperationContext, String... logicalKey) throws GeminiException {
        EntityRecord rec = RecordConverters.entityRecordFromMap(e, body);
        try {
            UUID uuid = UUID.fromString(logicalKey[0]);
            return entityManager.update(uuid, rec, entityOperationContext);
        } catch (IllegalArgumentException e1) {
            // it is not a UUID
            List<EntityFieldValue> logicalKeyValues = RecordConverters.logicalKeyFromStrings(e, logicalKey);
            return entityManager.update(logicalKeyValues, rec, entityOperationContext);
        }
    }

    private EntityRecord handleDeleteRecord(Entity entity, EntityOperationContext entityOperationContext, String... logicalKey) throws GeminiException {
        try {
            UUID uuid = UUID.fromString(logicalKey[0]);
            return entityManager.delete(entity, uuid, entityOperationContext);
        } catch (IllegalArgumentException e1) {
            List<EntityFieldValue> logicalKeyValues = RecordConverters.logicalKeyFromStrings(entity, logicalKey);
            return entityManager.delete(entity, logicalKeyValues, entityOperationContext);
        }
    }

    private EntityRecord handleGetRecord(Entity e, EntityOperationContext entityOperationContext, String... logicalKey) throws GeminiException {
        try {
            UUID uuid = UUID.fromString(logicalKey[0]);
            return entityManager.get(e, uuid);
        } catch (IllegalArgumentException e1) {
            List<EntityFieldValue> logicalKeyValues = RecordConverters.logicalKeyFromStrings(e, logicalKey);
            return entityManager.get(e, logicalKeyValues);
        }
    }

    /* private EntityRecord handleGetRecord(Entity e, String logicalKey) throws SQLException, GeminiException {
        Set<DynamicRecord.FieldValue> logicalKeyValue = Set.of(Field.RecordConverters.logicalKeyFromStrings(e, logicalKey));
        return entityManager.get(e, logicalKeyValue);
    } */

    private Entity checkEntity(String entityStr) throws EntityException {
        Entity entity = entityManager.getEntity(entityStr);
        if (entity == null) {
            throw EntityException.ENTITY_NOT_FOUND(entityStr.toUpperCase());
        }
        if (entity.isEmbedable()) {
            throw EntityException.API_NOT_ALLOWED_ON_EMBEDABLE(entityStr.toUpperCase());
        }
        return entity;
    }

    private void ensurePathsAreConsistent(List<String> paths, Entity e) {
        assert e != null;
        assert paths.size() >= 2;
        assert paths.get(0).equals("api");
        assert paths.get(1).toUpperCase().equals(e.getName().toUpperCase());
    }
}
