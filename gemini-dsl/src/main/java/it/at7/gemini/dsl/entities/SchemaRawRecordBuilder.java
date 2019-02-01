package it.at7.gemini.dsl.entities;

import java.util.ArrayList;
import java.util.List;

public class SchemaRawRecordBuilder {
    private final String entity;
    private final List<Object> records;
    private Object def;

    public SchemaRawRecordBuilder(String entity) {
        this.entity = entity;
        this.def = null;
        this.records = new ArrayList<>();
    }

    public void setDefaultRecord(Object def){
        assert this.def == null;
        this.def = def;
    }

    public void addRecord(Object record){
        records.add(record);
    }

    public void addRecords(List<Object> listRecord) {
        records.addAll(listRecord);
    }

    public SchemaRawRecords build(){
        return new SchemaRawRecords(entity, def, records);
    }
}
