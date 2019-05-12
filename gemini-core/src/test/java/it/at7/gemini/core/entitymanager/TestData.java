package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.SchemaManager;
import it.at7.gemini.core.Services;
import it.at7.gemini.schema.Entity;

public class TestData {

    public static Entity getTestDataTypeEntity() {
        SchemaManager schemaManager = Services.getSchemaManager();
        return schemaManager.getEntity("TestDataType");
    }

    /**
     * DynamicRecord withGeminiSearchString primitive dataType
     */
    public static EntityRecord getTestDataTypeEntityRecord(String logicalKey) {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity testDataType = schemaManager.getEntity("TestDataType");
        EntityRecord entityRecord = new EntityRecord(testDataType);
        entityRecord.put("text", logicalKey);
        return entityRecord;
    }

    public static EntityRecord getDomain_singlelk_EntityRecord(String domainLogicalKey){
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity testDomain1 = schemaManager.getEntity("TestDomain1");
        EntityRecord entityRecord = new EntityRecord(testDomain1);
        entityRecord.put("code", domainLogicalKey);
        return entityRecord;
    }

    public static EntityRecord getEmbedable_singlelk_EntityRecord(String domainLogicalKey){
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity et = schemaManager.getEntity("TestDataTypeEmbeded");
        EntityRecord entityRecord = new EntityRecord(et);
        entityRecord.put("code", domainLogicalKey);
        return entityRecord;
    }

}
