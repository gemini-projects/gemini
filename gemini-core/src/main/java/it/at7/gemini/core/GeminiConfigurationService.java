package it.at7.gemini.core;

import it.at7.gemini.conf.DynamicSchema;
import it.at7.gemini.conf.SchemaMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiConfigurationService {

    @Value("${gemini.dynamicSchema:DISABLED}")
    private String dynamicSchema;

    public DynamicSchema getDynamicSchema() {
        return DynamicSchema.valueOf(this.dynamicSchema);
    }

    @Value("${gemini.api.list.limit:100}")
    private int apiListLimit;

    public int getApiListLimit() {
        return apiListLimit;
    }

    @Value("${gemini.api.openapi.schema:false}")
    private boolean openapiSchema;

    public boolean isOpenapiSchema() {
        return openapiSchema;
    }

    @Value("${gemini.api.openapi.dir:./openapi/schema}")
    private String openApiDir;

    public String getOpenApiDir() {
        return openApiDir;
    }

    @Value("${gemini.core.serviceInfoPath:./service/info.yml}")
    private String serviceInfoResource;

    public String getServiceInfoResource() {
        return serviceInfoResource;
    }

    @Value("${gemini.dev-mode:false}")
    private boolean devMode;

    public boolean isDevMode() {
        return devMode;
    }

    @Value("${gemini.schema.mode:UPDATE}")
    private String schemaMode;

    public SchemaMode getSchemaMode() {
        return SchemaMode.valueOf(this.schemaMode);
    }

}
