package it.at7.gemini.dsl.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRawRecordBuilder {
    private final String entity;
    private Object def;
    private Map<String, EntityRawRecords.VersionedRecords> versionedRecordsList;

    public EntityRawRecordBuilder(String entity) {
        this.entity = entity;
        this.def = null;
        this.versionedRecordsList = new HashMap<>();
    }

    public void setDefaultRecord(Object def) {
        if (this.def != null) {
            throw new RuntimeException(String.format("Two DEFAULT definition not allowed for: %s", entity));
        }
        this.def = def;
    }

    public void addRecord(String versionName, long versionProgressive, Object record, long definitionOrder) {
        addRecords(versionName, versionProgressive, List.of(record), definitionOrder);
    }

    public EntityRawRecordBuilder addRecords(String versionName, long versionProgressive, List<Object> listRecord, long definitionOrder) {
        if (versionedRecordsList.containsKey(versionName.toUpperCase())) {
            throw new RuntimeException(String.format("Two Entity Records definitions with the same version Name are not allowed: %s - %s", entity, versionName));
        }
        EntityRawRecords.VersionedRecords vr = new EntityRawRecords.VersionedRecords(entity, versionName, versionProgressive, listRecord, definitionOrder);
        this.versionedRecordsList.put(versionName, vr);
        return this;
    }

    public EntityRawRecords build() {
        return new EntityRawRecords(entity, def, versionedRecordsList);
    }

}
