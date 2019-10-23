package it.at7.gemini.schema.smart;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.SmartModule;
import it.at7.gemini.dsl.entities.RawEntity;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.dsl.entities.RawSchemaBuilder;
import it.at7.gemini.gui.schema.EntityGUIRef;
import it.at7.gemini.gui.schema.FieldGUIRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SmartSchema {
    public String title;
    public String description;

    public Map<String, SmartEntity> entities;


    public RawSchema getRawSchema(SmartModule smartModule) {
        RawSchemaBuilder rawSchemaBuilder = new RawSchemaBuilder();
        if (entities != null)
            entities.forEach((key, value) -> createRowEntity(rawSchemaBuilder, smartModule, key, value));
        return rawSchemaBuilder.build();
    }

    private void createRowEntity(RawSchemaBuilder rawSchemaBuilder, SmartModule smartModule, String entityName, SmartEntity sEntity) {
        String fullEntityName = entityName(smartModule, entityName);
        RawEntity rawEntity = sEntity.toRawEntity(fullEntityName);
        rawSchemaBuilder.addEntity(rawEntity);
    }

    public List<EntityRecord> getEntityGUIRecords(SmartModule smartModule) {
        return entities == null ? List.of() : entities.entrySet().stream().map(e -> creteEntityGUI(smartModule, e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public List<EntityRecord> getFieldGUIRecords(SmartModule smartModule) {
        return entities == null ? List.of() : entities.entrySet().stream().map(e -> creteFieldGUI(smartModule, e.getKey(), e.getValue()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private EntityRecord creteEntityGUI(SmartModule smartModule, String entityName, SmartEntity smartEntity) {
        String fullEntityName = entityName(smartModule, entityName);
        return EntityGUIRef.record(fullEntityName, smartEntity.displayName);
    }

    private List<EntityRecord> creteFieldGUI(SmartModule smartModule, String entityName, SmartEntity smartEntity) {
        String fullEntityName = entityName(smartModule, entityName);
        List<EntityRecord> records = new ArrayList<>();
        for (Map.Entry<String, SmartField> fieldEntry : smartEntity.fields.entrySet()) {
            String fieldName = fieldEntry.getKey();
            String displayName = fieldEntry.getValue().displayName;
            records.add(FieldGUIRef.record(fullEntityName, fieldName, displayName));
        }
        return records;
    }


    private String entityName(SmartModule smartModule, String name) {
        String schemaPrefix = smartModule.getSchemaPrefix();
        return schemaPrefix.concat(name);
    }
}
