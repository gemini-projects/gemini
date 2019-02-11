package it.at7.gemini.api;

import it.at7.gemini.core.EntityRecord;

import java.util.Collection;

class GeminiWrappers {
    public static class EntityRecordsList {
        Collection<EntityRecord> records;

        public EntityRecordsList(Collection<EntityRecord> records) {
            this.records = records;
        }

        public Collection<EntityRecord> getRecords() {
            return records;
        }

        public static EntityRecordsList of(Collection<EntityRecord> records) {
            return new EntityRecordsList(records);
        }
    }

    public static class EntityRecordApiType {
        EntityRecord record;

        public EntityRecordApiType(EntityRecord record) {
            this.record = record;
        }

        public EntityRecord get(){
            return record;
        }

        public static EntityRecordApiType of(EntityRecord record) {
            return new EntityRecordApiType(record);
        }
    }

    public static class EntityRecordListApiType {
        EntityRecordsList records;

        public EntityRecordListApiType(EntityRecordsList records) {
            this.records = records;
        }

        public EntityRecordsList getEntityRecordList() {
            return records;
        }

        public static EntityRecordListApiType of(EntityRecordsList recordList) {
            return new EntityRecordListApiType(recordList);
        }
    }
}
