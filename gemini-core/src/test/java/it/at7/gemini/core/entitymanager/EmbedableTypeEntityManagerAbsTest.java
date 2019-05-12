package it.at7.gemini.core.entitymanager;

import it.at7.gemini.UnitTestNoMockWeb;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static it.at7.gemini.core.entitymanager.EntityTestUtility.*;
import static org.junit.Assert.*;

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
        EntityRecord embededRecord = testEntity.get("embeded");
        Assert.assertNotNull(embededRecord);
        testDefaulValues(embededRecord, "");
        testDefaultMetaValues(testEntity);
    }

    @Test
    public void n2_putWithAllBasicTypes() throws GeminiException {

        // create embedable <--> with basic types for embeded record

        Entity embedableEntity = Services.getSchemaManager().getEntity("EmbedableEntity");
        EntityRecord embedable = new EntityRecord(embedableEntity);
        embedable.put("text", "logKey-allBasicTypes");
        embedable.put("numberLong", 10);
        embedable.put("numberDouble", 11.1);
        embedable.put("long", 10);
        embedable.put("double", 11.1);
        embedable.put("bool", true);
        embedable.put("date", LocalDate.of(1989, 6, 9));
        embedable.put("time", LocalTime.of(7, 7, 7));
        embedable.put("datetime", LocalDateTime.of(1989, 6, 9, 7, 7, 7));
        embedable.put("textarray", new String[]{"abc", "def"});
        EntityRecord entityRecord = TestData.getEmbedable_singlelk_EntityRecord("logKey-withSomeEbdValues");
        entityRecord.put("embeded", embedable);
        EntityRecord testEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        EntityRecord embededRecord = testEntity.get("embeded");
        checkValuesForEmbeded(embededRecord);
        testDefaultMetaValues(testEntity);

    }

    @Test
    public void n3_getWithEmbeded() throws GeminiException {
        EntityRecord recWithLK = TestData.getEmbedable_singlelk_EntityRecord("logKey-withSomeEbdValues");
        EntityRecord fullRecord = Services.getEntityManager().get(recWithLK);
        EntityRecord embededRecord = fullRecord.get("embeded");
        checkValuesForEmbeded(embededRecord);
        testDefaultMetaValues(fullRecord);
    }

    @Test
    public void n4_updateEmbeded() throws GeminiException {
        EntityRecord recWithLK = TestData.getEmbedable_singlelk_EntityRecord("logKey-withSomeEbdValues");
        EntityRecord fullRecord = Services.getEntityManager().get(recWithLK);
        EntityRecord embededRecord = fullRecord.get("embeded");
        embededRecord.put("numberLong", 11);
        EntityRecord updatedRecord = Services.getEntityManager().update(fullRecord);
        EntityRecord embededRecord2 = updatedRecord.get("embeded");
        assertEquals(11, (long) embededRecord2.get("numberLong"));
        Object idOrig = embededRecord.getID();
        Object idUpdated = embededRecord2.getID();
        idOrig.equals(idUpdated);
        checkMetaModifiedChanged(updatedRecord);
    }

    @Test
    public void n5_updateEmbededWithoutGettingItFirst() throws GeminiException {
        EntityRecord recWithLK = TestData.getEmbedable_singlelk_EntityRecord("logKey-withSomeEbdValues");
        EntityRecord fullRecord = Services.getEntityManager().get(recWithLK);
        EntityRecord embededRecord = fullRecord.get("embeded");
        Object idOrig = embededRecord.getID();


        // update directly an available record with embeded subrecords..
        // if we don't get it we don't have the ID filled.. so the software should not recreate the embeded field
        // lets change only the numberLong for the already existent embeded entity
        Entity embedableEntity = Services.getSchemaManager().getEntity("EmbedableEntity");
        EntityRecord embedable = new EntityRecord(embedableEntity);
        embedable.put("text", "logKey-allBasicTypes");
        embedable.put("numberLong", 20);
        recWithLK.put("embeded", embedable);

        EntityRecord updatedRecord = Services.getEntityManager().update(recWithLK);
        EntityRecord embededRecord2 = updatedRecord.get("embeded");
        assertEquals(20, (long) embededRecord2.get("numberLong"));
        Object idUpdated = embededRecord2.getID();
        idOrig.equals(idUpdated);
        checkMetaModifiedChanged(updatedRecord);
    }

    @Test
    public void n6_deleteEmbeded() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        EntityRecord recWithLK = TestData.getEmbedable_singlelk_EntityRecord("logKey-withSomeEbdValues");
        EntityRecord fullRecord = entityManager.get(recWithLK);
        EntityRecord deleted = entityManager.delete(fullRecord);
        EntityRecord deletedEmbeded = deleted.get("embeded");
        assertEquals(20, (long) deletedEmbeded.get("numberLong")); // updated on test n5

        Services.getTransactionManager().executeInSingleTrasaction(t -> {
            Optional<EntityRecord> deletedRecord = Services.getPersistenceEntityManager().getEntityRecordById(deleted.getEntity(), (long) deleted.getID(), t);
            assertFalse(deletedRecord.isPresent());
            Optional<EntityRecord> delEmb = Services.getPersistenceEntityManager().getEntityRecordById(deletedEmbeded.getEntity(), (long) deletedEmbeded.getID(), t);
            assertFalse(delEmb.isPresent());

        });
    }

    private void checkValuesForEmbeded(EntityRecord embededRecord) {
        Assert.assertNotNull(embededRecord);
        assertEquals("logKey-allBasicTypes", embededRecord.get("text")); // real field
        assertEquals(10, (long) embededRecord.get("numberLong"));
        assertEquals(10, (long) embededRecord.get("long"));
        assertEquals(11.1, embededRecord.get("numberDouble"), 0.01);
        assertEquals(11.1, embededRecord.get("double"), 0.01);
        assertEquals(LocalDate.of(1989, 6, 9), embededRecord.get("date"));
        assertEquals(LocalTime.of(7, 7, 7), embededRecord.get("time"));
        assertEquals(LocalDateTime.of(1989, 6, 9, 7, 7, 7), embededRecord.get("datetime"));
        assertEquals(true, embededRecord.get("bool"));
        assertArrayEquals(new String[]{"abc", "def"}, embededRecord.get("textarray"));
    }
}
