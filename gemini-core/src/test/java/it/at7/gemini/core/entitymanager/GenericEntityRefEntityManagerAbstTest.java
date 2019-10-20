package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static it.at7.gemini.core.entitymanager.EntityTestUtility.testDefaultMetaValues;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenericEntityRefEntityManagerAbstTest {
    @Test
    public void n1_putIfAbsent() throws GeminiException {
        EntityRecord lk1 = TestData.getDomain_singlelk_EntityRecord("lk1");
        lk1 = Services.getEntityManager().putOrUpdate(lk1);

        // put a simple logical key (string) -- others values are default
        EntityRecord entityRecord = TestData.getEntityRef_EntityRecord("logKey");
        entityRecord.put("genericRef", lk1);
        EntityRecord testRec = Services.getEntityManager().putIfAbsent(entityRecord);
        testDefaultMetaValues(testRec);

        assertEquals("logKey", testRec.get("logKey"));
        EntityReferenceRecord erf = testRec.get("genericRef");
        assertEquals(Services.getSchemaManager().getEntity("TestDomain1"), erf.getEntity());
        assertEquals("lk1", erf.getLogicalKeyRecord().get("code"));
        assertEquals(lk1.getID(), erf.getPrimaryKey());
    }

    @Test
    public void n2_updateExistent() throws GeminiException {
        EntityRecord entityRecord = TestData.getEntityRef_EntityRecord("logKey");
        entityRecord = Services.getEntityManager().get(entityRecord);

        assertEquals("logKey", entityRecord.get("logKey"));
        EntityReferenceRecord erf = entityRecord.get("genericRef");
        assertEquals(Services.getSchemaManager().getEntity("TestDomain1"), erf.getEntity());
        assertEquals("lk1", erf.getLogicalKeyRecord().get("code"));

        EntityRecord lk2 = TestData.getDomain_singlelk_EntityRecord("lk2");
        lk2 = Services.getEntityManager().putOrUpdate(lk2);

        entityRecord.put("genericRef", lk2);
        EntityRecord testRec = Services.getEntityManager().putOrUpdate(entityRecord);
        assertEquals("logKey", testRec.get("logKey"));
        erf = testRec.get("genericRef");
        assertEquals(Services.getSchemaManager().getEntity("TestDomain1"), erf.getEntity());
        assertEquals("lk2", erf.getLogicalKeyRecord().get("code"));
        assertEquals(lk2.getID(), erf.getPrimaryKey());
    }

    @Test
    public void n3_testRowWithDifferentEntityRef() throws GeminiException {
        // add anothre entity ref to the one of test n1 and n2

        EntityManager entityManager = Services.getEntityManager();
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("bT-logKey");
        EntityRecord tER = entityManager.putIfAbsent(entityRecord);

        EntityRecord entityRecordG = TestData.getEntityRef_EntityRecord("logKey-2");
        entityRecordG.put("genericRef", tER);
        entityManager.putIfAbsent(entityRecordG);

        List<EntityRecord> listGenericRecs = entityManager.getRecordsMatching(entityManager.getEntity("WithGenericEntityRef"), new FilterContext(FilterContext.FilterType.GEMINI, "", 10, 0, null, false, Map.of()));
        assertEquals(2, listGenericRecs.size());

    }
}

