package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static it.at7.gemini.core.entitymanager.BasicTypesEntityManagerAbstTest.testDefaulValues;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class EmbedableTypeEntityManagerAbsTest {

    @Test
    public void n1_putIfAbsent() throws GeminiException {

        // default should be null
        EntityRecord entityRecord = TestData.getEmbedable_singlelk_EntityRecord("logKey_def");
        EntityRecord testEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        Assert.assertNull(testEntity.get("embeded"));

        // create embedable <--> with default value
        Entity embedableEntity = Services.getSchemaManager().getEntity("EmbedableEntity");
        EntityRecord embedable = new EntityRecord(embedableEntity);
        entityRecord = TestData.getEmbedable_singlelk_EntityRecord("logKey");
        entityRecord.put("embeded", embedable);
        testEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        EntityRecord embededRecord  = testEntity.get("embeded");
        Assert.assertNotNull(embededRecord);
        testDefaulValues(embededRecord, "");






    }
}
