package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.EntityReferenceRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collection;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityRefArrayEntityManagerAbstTest {
    static EntityRecord lk1;
    static EntityRecord lk2;

    public static void insertDomainRecords() throws GeminiException {
        lk1 = TestData.getDomain_singlelk_EntityRecord("lk1");
        lk2 = TestData.getDomain_singlelk_EntityRecord("lk2");
        lk1 = Services.getEntityManager().putOrUpdate(lk1);
        lk2 = Services.getEntityManager().putOrUpdate(lk2);
    }

    @Test
    public void n1_putIfAbsent_WithLkString() throws GeminiException {
        insertDomainRecords();
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-erefarray");
        List<EntityRecord> domain1Array = List.of(lk1);
        entityRecord.put("domain1Array", domain1Array);
        EntityRecord persistedEntity = Services.getEntityManager().putIfAbsent(entityRecord);
        Collection<EntityReferenceRecord> domainPkRefArray = persistedEntity.get("domain1Array");
        Assert.assertEquals(1, domainPkRefArray.size());
        EntityReferenceRecord lk1Ref = domainPkRefArray.iterator().next();
        Assert.assertEquals(lk1.getID(), lk1Ref.getPrimaryKey());
        Assert.assertEquals(lk1.getLogicalKeyValue(), lk1Ref.getLogicalKeyRecord().getFieldValues());
    }

    @Test
    public void n2_addAnotherRefInsideArray() throws GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-erefarray");
        List<EntityRecord> domain1Array = List.of(lk1, lk2);
        entityRecord.put("domain1Array", domain1Array);
        EntityRecord persistedEntity = Services.getEntityManager().update(entityRecord);
        Collection<EntityReferenceRecord> domainPkRefArray = persistedEntity.get("domain1Array");
        Assert.assertEquals(2, domainPkRefArray.size());
    }

    @Test
    public void n3_eraseRefArray() throws GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-erefarray");
        List<EntityRecord> domain1Array = List.of();
        entityRecord.put("domain1Array", domain1Array);
        EntityRecord persistedEntity = Services.getEntityManager().update(entityRecord);
        Collection<EntityReferenceRecord> domainPkRefArray = persistedEntity.get("domain1Array");
        Assert.assertEquals(0, domainPkRefArray.size());
    }

    @Test(expected = EntityRecordException.class)
    public void n4_ExceptionIfNotFound() throws GeminiException {
        EntityRecord lk5 = TestData.getDomain_singlelk_EntityRecord("lk5");
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey");
        List<EntityRecord> domain1Array = List.of(lk5);
        entityRecord.put("domain1Array", domain1Array);
        Services.getEntityManager().update(entityRecord);
    }

}
