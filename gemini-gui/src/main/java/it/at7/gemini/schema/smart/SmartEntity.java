package it.at7.gemini.schema.smart;

import it.at7.gemini.dsl.entities.RawEntity;
import it.at7.gemini.dsl.entities.RawEntityBuilder;

import java.util.Map;

public class SmartEntity {
    public String displayName;
    public Map<String, SmartField> fields;


    public RawEntity toRawEntity(String entityName) {
        RawEntityBuilder builder = new RawEntityBuilder();
        builder.addName(entityName);
        if (fields != null)
            fields.entrySet().forEach(f -> builder.addEntry(f.getValue().toEntry(builder, f.getKey())));
        return builder.build();
    }
}
