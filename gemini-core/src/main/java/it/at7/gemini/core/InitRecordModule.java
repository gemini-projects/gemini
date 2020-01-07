package it.at7.gemini.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ModuleDescription(
        name = "INIT_RECORDS",
        dependencies = "CORE",
        order = 1)
@ConditionalOnProperty(name = "gemini.modules.init_records", havingValue = "true")
public class InitRecordModule implements GeminiModule {

    @Override
    public String getSchemaResourceLocation() {
        return null;
    }

    @Override
    public boolean createSchemaIfNotFound() {
        return false;
    }

    @Override
    public String getSchemaLocation() {
        return null;
    }

    @Override
    public String getEntityRecordResourceLocation(String entityName) {
        String pattern = "file:./init/records_%s.atr";
        return String.format(pattern, entityName);
    }

    @Override
    public String getSchemaRecordResourceLocation() {
        return "file:./init/records.atr";
    }
}
