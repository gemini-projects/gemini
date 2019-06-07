package it.at7.gemini.core;

import it.at7.gemini.conf.DynamicSchema;
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
}
