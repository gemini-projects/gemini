package it.at7.gemini.core;

import it.at7.gemini.conf.DynamicSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {

    @Value("${gemini.dynamicSchema:DISABLED}")
    String dynamicSchema;

    public DynamicSchema getDynamicSchema() {
        return DynamicSchema.valueOf(this.dynamicSchema);
    }
}
