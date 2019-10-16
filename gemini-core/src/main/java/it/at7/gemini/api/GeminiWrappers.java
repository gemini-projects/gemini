package it.at7.gemini.api;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.FilterContext;

import java.util.Collection;

public class GeminiWrappers {
    public static class EntityRecordsList {
        private Collection<EntityRecord> records;
        private final FilterContext filterContext;

        public EntityRecordsList(Collection<EntityRecord> records, FilterContext filterContext) {
            this.records = records;
            this.filterContext = filterContext;
        }

        public EntityRecordsList(Collection<EntityRecord> records) {
            this(records, null);
        }

        public Collection<EntityRecord> getRecords() {
            return records;
        }

        public FilterContext getFilterContext() {
            return filterContext;
        }

        public static EntityRecordsList of(Collection<EntityRecord> records, FilterContext filterContext) {
            return new EntityRecordsList(records, filterContext);
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

        public EntityRecord get() {
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

    public static class CountRequest {
        private final long result;
        private final FilterContext filterContext;

        public CountRequest(long result, FilterContext filterContext) {
            this.result = result;
            this.filterContext = filterContext;
        }

        public long getResult() {
            return result;
        }

        public FilterContext getFilterContext() {
            return filterContext;
        }

        public static CountRequest of(long result, FilterContext filterContext) {
            return new CountRequest(result, filterContext);
        }
    }

    public static class CountRequestApiType {
        private final CountRequest countRequest;

        public CountRequestApiType(CountRequest countRequest) {
            this.countRequest = countRequest;
        }

        public CountRequest getCountRequest() {
            return countRequest;
        }

        public static CountRequestApiType of(CountRequest countRequest) {
            return new CountRequestApiType(countRequest);
        }
    }
}
