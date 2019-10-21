package it.at7.gemini.schema;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.RecordConverters;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.SmartModule;

import java.util.HashMap;
import java.util.Map;

public class SmartModuleEntity {
    private final String name;
    private final boolean dynamic;
    private final String smartSchemaString;

    public SmartModuleEntity(String name, boolean dynamic, String smartSchemaString) {
        this.name = name;
        this.dynamic = dynamic;
        this.smartSchemaString = smartSchemaString;
    }

    public static SmartModuleEntity of(SmartModule smartModule, String smartSchemaString) {
        return new SmartModuleEntity(smartModule.getName().toUpperCase(), smartModule.isDynamic(), smartSchemaString);
    }

    public EntityRecord toEntityRecord() {
        Map<String, Object> values = new HashMap<>();
        values.put(SmartModuleEntityRef.FIELDS.NAME, name);
        values.put(SmartModuleEntityRef.FIELDS.DYNAMIC, dynamic);
        values.put(SmartModuleEntityRef.FIELDS.SCHEMA, smartSchemaString);
        Entity entity = Services.getSchemaManager().getEntity(SmartModuleEntityRef.NAME);
        assert entity != null;
        return RecordConverters.entityRecordFromMap(entity, values);
    }
}
