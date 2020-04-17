package it.at7.gemini.core.entitymanager;


import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityRef;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityCacheManagerAbstTest {

    @Test
    public void n1_putElementAndCheckCache() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();

        // put a simple logical key (string) -- others values are default
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-ecachetest");
        entityManager.putIfAbsent(entityRecord);

        Entity cacheEntity = entityManager.getEntity(EntityRef.ERA.NAME);
        Entity dtEntity = entityManager.getEntity("TestDataType");
        PersistenceEntityManager persistenceEntityManager = Services.getPersistenceEntityManager();
        LocalDateTime ldt1 = Services.getTransactionManager().executeEntityManagedTransaction(t ->
                {
                    Optional<EntityRecord> cacheRec =
                            persistenceEntityManager.getEntityRecordById(cacheEntity, (long) dtEntity.getIDValue(), t);
                    Assert.assertTrue(cacheRec.isPresent());
                    EntityRecord record = cacheRec.get();
                    return record.get(EntityRef.ERA.FIELDS.TIMESTAMP);

                }
        );

        entityRecord.put("text", "updated");
        entityManager.update(entityRecord);
        LocalDateTime ldt2 = Services.getTransactionManager().executeEntityManagedTransaction(t ->
                {
                    Optional<EntityRecord> cacheRec =
                            persistenceEntityManager.getEntityRecordById(cacheEntity, (long) dtEntity.getIDValue(), t);
                    Assert.assertTrue(cacheRec.isPresent());
                    EntityRecord record = cacheRec.get();
                    return record.get(EntityRef.ERA.FIELDS.TIMESTAMP);

                }
        );
        Assert.assertTrue(ldt2.isAfter(ldt1));
    }

    @Test
    public void n2_getALLUpdated() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        Map<Entity, LocalDateTime> entitiesLastUpdate = entityManager.getEntitiesLastUpdate();
        Entity dtEntity = entityManager.getEntity("TestDataType");
        Assert.assertTrue(entitiesLastUpdate.containsKey(dtEntity));
        Assert.assertNotNull(entitiesLastUpdate.get(dtEntity));
    }

}