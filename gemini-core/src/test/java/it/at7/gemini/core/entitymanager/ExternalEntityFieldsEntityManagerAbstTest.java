package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static it.at7.gemini.core.entitymanager.EntityTestUtility.testDefaulValues;
import static it.at7.gemini.core.entitymanager.EntityTestUtility.testDefaultMetaValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExternalEntityFieldsEntityManagerAbstTest {

    @Test
    public void n1_externalField() throws GeminiException {
        // Put a simple logical key (string) -- others values are default
        EntityRecord entityRecord = TestData.getTestDataTypeExternalEntityRecord("logKey");
        // add also the external field
        entityRecord.put("externalText", "testExternalFieldOnOtherModule");
        EntityRecord testEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        testDefaulValues(testEntity, "logKey");
        assertEquals("testExternalFieldOnOtherModule", testEntity.get("externalText")); // real field
        testDefaultMetaValues(testEntity);
        assertNotNull(testEntity.get("externalMetaField"));
    }

    @Test
    public void n2_externalInterfaceWithExternalField() throws GeminiException {
        // This is the entity that implements an interface and then is extended in other module (implementing another interface)
        Entity entityWithInterfaceToExtend = Services.getSchemaManager().getEntity("EntityWithInterfaceToExtend");
        EntityRecord er = new EntityRecord(entityWithInterfaceToExtend);
        er.put("entityText", "extLk");
        er.put("interfaceText", "interfaceITModule");
        er.put("externalIntText", "extInterfaceITEModule");
        er.put("externalEntityText", "extEntityField");
        EntityRecord inserted = Services.getEntityManager().putIfAbsent(er);
        assertEquals("extLk", inserted.get("entityText")); // real field
        assertEquals("interfaceITModule", inserted.get("interfaceText")); // real field
        assertEquals("extInterfaceITEModule", inserted.get("externalIntText")); // real field
        assertEquals("extEntityField", inserted.get("externalEntityText")); // real field
        testDefaultMetaValues(inserted);
    }

}
