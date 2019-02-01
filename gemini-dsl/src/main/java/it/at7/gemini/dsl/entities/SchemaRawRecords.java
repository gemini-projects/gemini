package it.at7.gemini.dsl.entities;

import java.util.List;

public class SchemaRawRecords {
    private final String entity;
    private final Object def;
    private final List<Object> records;

    public SchemaRawRecords(String entity, Object def, List<Object> records) {
        this.entity = entity;
        this.def = def;
        this.records = records;
    }

    public String getEntity() {
        return entity;
    }

    public Object getDef() {
        return def;
    }

    public List<Object> getRecords() {
        return records;
    }
}
