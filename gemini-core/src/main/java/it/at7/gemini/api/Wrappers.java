package it.at7.gemini.api;

import it.at7.gemini.core.EntityRecord;

import java.util.Collection;

class Wrappers {
    public static class EntityRecordsListWrapper {
        Collection<EntityRecord> records;

        public EntityRecordsListWrapper(Collection<EntityRecord> records) {
            this.records = records;
        }

        public Collection<EntityRecord> getRecords() {
            return records;
        }

        public static EntityRecordsListWrapper of(Collection<EntityRecord> records) {
            return new EntityRecordsListWrapper(records);
        }
    }

}
