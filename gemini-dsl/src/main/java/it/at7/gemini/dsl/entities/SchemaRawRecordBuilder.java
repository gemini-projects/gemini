package it.at7.gemini.dsl.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaRawRecordBuilder {
    private final String entity;
    private Object def;
    private Map<String, SchemaRawRecords.VersionedRecords> versionedRecordsList;

    public SchemaRawRecordBuilder(String entity) {
        this.entity = entity;
        this.def = null;
        this.versionedRecordsList = new HashMap<>();
    }

    public SchemaRawRecordBuilder setDefaultRecord(long versionProgressive, Object def) {
        if (this.def != null) {
            throw new RuntimeException(String.format("Two DEFAULT definition not allowed for: %s", entity));
        }
        this.def = def;
        return this;
    }

    public void addRecord(String versionName, long versionProgressive, Object record) {
        addRecords(versionName, versionProgressive, List.of(record));
    }

    public SchemaRawRecordBuilder addRecords(String versionName, long versionProgressive, List<Object> listRecord) {
        if (versionedRecordsList.containsKey(versionName.toUpperCase())) {
            throw new RuntimeException(String.format("Two Entity Records definitions with the same version Name are not allowed: %s - %s", entity, versionName));
        }
        SchemaRawRecords.VersionedRecords vr = new SchemaRawRecords.VersionedRecords(versionName, versionProgressive, listRecord);
        this.versionedRecordsList.put(versionName, vr);
        return this;
    }

    public SchemaRawRecords build() {
        return new SchemaRawRecords(entity, def, versionedRecordsList);
    }


}
