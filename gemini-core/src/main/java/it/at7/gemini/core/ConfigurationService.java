package it.at7.gemini.core;

import it.at7.gemini.conf.DynamicSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

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
}
