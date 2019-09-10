package it.at7.gemini.dsl.entities;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EntityRawRecords {
    private final String entity;
    private final Object defaultRecord;
    private final Map<String, VersionedRecords> records;

    public EntityRawRecords(String entity, Object defaultRecord, Map<String, VersionedRecords> records) {
        this.entity = entity;
        this.defaultRecord = defaultRecord;
        this.records = records;
    }

    public String getEntity() {
        return entity;
    }

    @Nullable
    public Object getDefaultRecord() {
        return defaultRecord;
    }

    public Map<String, VersionedRecords> getVersionedRecords() {
        return records;
    }

    public static class VersionedRecords {
        private final String entity;
        private final String versionName;
        private final long versionProgressive;
        private final List<Object> records;

        private final long definitionOrder;

        public VersionedRecords(String entity, String versionName, long versionProgressive, List<Object> records, long definitionOrder) {
            this.entity = entity;

            this.versionName = versionName;
            this.versionProgressive = versionProgressive;
            this.records = records;
            this.definitionOrder = definitionOrder;
        }

        public VersionedRecords(String entity, String versionName, long versionProgressive, Object record, long definitionOrder) {
            this(entity, versionName, versionProgressive, List.of(record), definitionOrder);
        }

        public String getEntity() {
            return entity;
        }

        public String getVersionName() {
            return versionName;
        }

        public long getVersionProgressive() {
            return versionProgressive;
        }

        public List<Object> getRecords() {
            return records;
        }

        public long getDefinitionOrder() {
            return definitionOrder;
        }
    }
}
