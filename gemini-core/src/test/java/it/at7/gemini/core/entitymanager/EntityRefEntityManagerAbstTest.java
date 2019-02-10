package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.DynamicRecord;
import it.at7.gemini.core.EntityReferenceRecord;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.SQLException;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class EntityRefEntityManagerAbstTest {
    public static void insertDomainRecords() throws SQLException, GeminiException {
        EntityRecord lk1 = TestData.getDomain_singlelk_EntityRecord("lk1");
        EntityRecord lk2 = TestData.getDomain_singlelk_EntityRecord("lk2");
        Services.getEntityManager().putOrUpdate(lk1);
        Services.getEntityManager().putOrUpdate(lk2);
    }

    @Test
    public void n1_putIfAbsent_WithLkString() throws SQLException, GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey");
        entityRecord.put("domain1", "lk1");
        EntityRecord persistedEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        Object domainPkRefObj = persistedEntity.get("domain1");
        Assert.assertTrue(domainPkRefObj instanceof EntityReferenceRecord);
        EntityReferenceRecord domainPkRef = (EntityReferenceRecord) domainPkRefObj;
        Assert.assertTrue(domainPkRef.hasPrimaryKey());
        Assert.assertTrue(domainPkRef.hasLogicalKey());
        String domain1 = domainPkRef.getLogicalKeyRecord().get("code");
        Assert.assertEquals("lk1", domain1);
    }

    @Test
    public void n2_putIfAbsent_WithEntityRecordObject() throws SQLException, GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey2");
        EntityRecord lk1 = TestData.getDomain_singlelk_EntityRecord("lk1");
        entityRecord.put("domain1", lk1);
        EntityRecord persistedEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        Object domainPkRefObj = persistedEntity.get("domain1");
        Assert.assertTrue(domainPkRefObj instanceof EntityReferenceRecord);
        EntityReferenceRecord domainPkRef = (EntityReferenceRecord) domainPkRefObj;
        Assert.assertTrue(domainPkRef.hasPrimaryKey());
        Assert.assertTrue(domainPkRef.hasLogicalKey());
        String domain1 = domainPkRef.getLogicalKeyRecord().get("code");
        Assert.assertEquals("lk1", domain1);
    }

    @Test
    public void n3_putIfAbsent_WithRecordObject() throws SQLException, GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey3");
        DynamicRecord lk1 = new DynamicRecord();
        lk1.put("code", "lk1");
        entityRecord.put("domain1", lk1);
        EntityRecord persistedEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        Object domainPkRefObj = persistedEntity.get("domain1");
        Assert.assertTrue(domainPkRefObj instanceof EntityReferenceRecord);
        EntityReferenceRecord domainPkRef = (EntityReferenceRecord) domainPkRefObj;
        Assert.assertTrue(domainPkRef.hasPrimaryKey());
        Assert.assertTrue(domainPkRef.hasLogicalKey());
        String domain1 = domainPkRef.getLogicalKeyRecord().get("code");
        Assert.assertEquals("lk1", domain1);
    }

    @Test
    public void n4_getEntityRecordsMatchingReferenceField() throws SQLException, GeminiException {
        Entity testDataTypeEntity = TestData.getTestDataTypeEntity();
        DynamicRecord entityRef = new DynamicRecord();
        entityRef.put("domain1", "lk1");
        List<EntityRecord> recordsMatching = Services.getEntityManager().getRecordsMatching(testDataTypeEntity, entityRef);
        Assert.assertEquals(3, recordsMatching.size()); // we have inserted 3 record with lk domain
    }

}
