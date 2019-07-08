package it.at7.gemini.api.openapi;

import java.util.Map;

public interface OpenApiService {

    void addOAuth2PasswordFlow(String name, Map<String, Object> flowParameters);

    void secureAllEntities(String securitySchemaName);
}
