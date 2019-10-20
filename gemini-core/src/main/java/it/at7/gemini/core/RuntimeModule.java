package it.at7.gemini.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "RUNTIME",
        dependencies = "CORE",
        editable = true,
        order = 700)
@ConditionalOnProperty(name = "gemini.modules.runtime", matchIfMissing = false)
public class RuntimeModule implements GeminiModule {

    @Override
    public String getSchemaResourceLocation() {
        String pattern = "file:./schema/%s.at";
        return String.format(pattern, getName());
    }

    @Override
    public boolean createSchemaIfNotFound() {
        return true;
    }

    @Override
    public String getSchemaLocation() {
        String pattern = "./schema/%s.at";
        return String.format(pattern, getName());
    }

    @Override
    public String getEntityRecordResourceLocation(String entityName) {
        String pattern = "file:./records/%s.atr";
        return String.format(pattern, entityName);
    }

    @Override
    public String getSchemaRecordResourceLocation() {
        String pattern = "file:./records/%s.atr";
        return String.format(pattern, getName());
    }
}
