package it.at7.gemini.schema.smart;

import it.at7.gemini.dsl.entities.RawEntity;
import it.at7.gemini.dsl.entities.RawEntityBuilder;

public class SmartField {
    public String displayName;
    public SmartFieldType type;
    public Boolean lk;
    public Integer lkOrder;


    public RawEntity.Entry toEntry(RawEntityBuilder builder, String fieldName) {
        RawEntityBuilder.EntryBuilder entryBuilder = new RawEntityBuilder.EntryBuilder(builder, type.name(), fieldName);
        if (lk != null && lk) {
            int order = lkOrder != null ? lkOrder : 1;
            entryBuilder.isLogicalKey(order);
        }
        return entryBuilder.build();
    }
}
