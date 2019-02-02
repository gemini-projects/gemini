package it.at7.gemini.dsl.entities;

import java.util.List;
import java.util.Map;

public class SchemaRawRecords {
    private final String entity;
    private final Object def;
    private final Map<String, VersionedRecords> records;

    public SchemaRawRecords(String entity, Object def, Map<String, VersionedRecords> records) {
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

    public Map<String, VersionedRecords> getVersionedRecords() {
        return records;
    }

    public static class VersionedRecords {
        private final String versionName;
        private final long versionProgressive;
        private final List<Object> records;

        public VersionedRecords(String versionName, long versionProgressive, List<Object> records) {
            this.versionName = versionName;
            this.versionProgressive = versionProgressive;
            this.records = records;
        }

        public VersionedRecords(String versionName, long versionProgressive, Object record) {
            this(versionName, versionProgressive, List.of(record));
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
    }
}
