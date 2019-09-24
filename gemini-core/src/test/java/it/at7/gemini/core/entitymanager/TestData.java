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

    public static Entity getTestDataTypeFilterEntity() {
        SchemaManager schemaManager = Services.getSchemaManager();
        return schemaManager.getEntity("TestDataTypeFilter");
    }

    /**
     * TestDataType Entity withRecord Primitive dataTypes
     */
    public static EntityRecord getTestDataTypeEntityRecord(String logicalKey) {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity testDataType = schemaManager.getEntity("TestDataType");
        EntityRecord entityRecord = new EntityRecord(testDataType);
        entityRecord.put("text", logicalKey);
        return entityRecord;
    }

    /**
     * TestDataType Entity withRecord Primitive dataTypes
     */
    public static EntityRecord getTestDataTypeForFilterEntityRecord(String logicalKey) {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity testDataType = schemaManager.getEntity("TestDataTypeFilter");
        EntityRecord entityRecord = new EntityRecord(testDataType);
        entityRecord.put("text", logicalKey);
        return entityRecord;
    }

    /**
     * TestDataType Entity to test External Module Fields (added to the core one)
     */
    public static EntityRecord getTestDataTypeExternalEntityRecord(String logicalKey) {
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity testDataType = schemaManager.getEntity("TestDataTypeForExternal");
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

    public static EntityRecord getEntityRef_EntityRecord(String lk){
        SchemaManager schemaManager = Services.getSchemaManager();
        Entity et = schemaManager.getEntity("WithGenericEntityRef");
        EntityRecord entityRecord = new EntityRecord(et);
        entityRecord.put("logKey", lk);
        return entityRecord;
    }


}
