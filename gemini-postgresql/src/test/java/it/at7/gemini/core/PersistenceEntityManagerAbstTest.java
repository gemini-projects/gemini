package it.at7.gemini.core;

import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.IdFieldException;
import it.at7.gemini.schema.Entity;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistenceEntityManagerAbstTest {

    private static Entity dataTypeEntity;
    private static Entity domainEntity;
    private static TransactionManager transactionManager;
    private static PersistenceEntityManager persistenceEntityManager;

    @Test
    public void n1_TestBasicTypesAndDuplicates() throws GeminiException {
        dataTypeEntity = Services.getSchemaManager().getEntity("TESTDATATYPE");
        domainEntity = Services.getSchemaManager().getEntity("TESTDOMAIN1");
        assertNotNull(dataTypeEntity);
        transactionManager = Services.getTransactionManager();
        persistenceEntityManager = Services.getPersistenceEntityManager();

        EntityRecord newrec = new EntityRecord(dataTypeEntity);
        newrec.put("text", "textString");
        newrec.put("numberLong", 77);
        newrec.put("numberDouble", 77.7);
        newrec.put("bool", false);
        // save the new entity
        EntityRecord savedEntity =
                transactionManager.executeInSingleTrasaction(t -> {
                    // new record
                    EntityRecord entity = persistenceEntityManager.createNewEntityRecord(newrec, t);
                    assertEquals("textString", entity.get("text"));
                    assertEquals(Long.valueOf(77), entity.get("numberLong"));
                    assertEquals(Double.valueOf(77.7), entity.get("numberDouble"));
                    assertEquals(false, entity.get("bool"));
                    return entity;
                });


        // error if we try to save it a second time (index on logical key)
        transactionManager.executeInSingleTrasaction(t -> {
            // check duplicated
            try {
                persistenceEntityManager.createNewEntityRecord(newrec, t);
                fail("It should throw Duplicate Exception because logical key already exist");
            } catch (GeminiException e) {

            }
        });


        // update the record
        transactionManager.executeInSingleTrasaction(t -> {
            savedEntity.put("text", "textUpdatedKey");
            savedEntity.put("numberLong", 88);
            savedEntity.put("numberDouble", 88.88);
            savedEntity.put("bool", true);
            EntityRecord newSavedEntity = persistenceEntityManager.updateEntityRecordByID(savedEntity, t);
            assertEquals("textUpdatedKey", newSavedEntity.get("text"));
            assertEquals(Long.valueOf(88), newSavedEntity.get("numberLong"));
            assertEquals(Double.valueOf(88.88), newSavedEntity.get("numberDouble"));
            assertEquals(true, newSavedEntity.get("bool"));
            Optional<EntityRecord> updated = persistenceEntityManager.getEntityRecordByLogicalKey(savedEntity, t);
            assertTrue(updated.isPresent()); // and only one
        });


        // first record (lk) no longer exists - we have updated also the key
        transactionManager.executeInSingleTrasaction(t -> {
            Optional<EntityRecord> recordByLogicalKey = persistenceEntityManager.getEntityRecordByLogicalKey(newrec, t);
            assertFalse(recordByLogicalKey.isPresent());
        });


        // delete the record
        transactionManager.executeInSingleTrasaction(t -> {
            persistenceEntityManager.deleteEntityRecordByID(savedEntity, t);
            Optional<EntityRecord> deletedRecord = persistenceEntityManager.getEntityRecordByLogicalKey(newrec, t);
            assertFalse(deletedRecord.isPresent());
        });

        // test insert with ID
        EntityRecord recWithID = new EntityRecord(dataTypeEntity);
        recWithID.put("_id", 777);
        recWithID.put("text", "textStringWithID");
        transactionManager.executeInSingleTrasaction(t -> {
            // new record
            EntityRecord r = persistenceEntityManager.createNewEntityRecord(recWithID, t);
            assertEquals("textStringWithID", r.get("text"));
            assertEquals(Long.valueOf(777), r.get("_id"));
        });
        EntityRecord recWithIDIncremented = new EntityRecord(dataTypeEntity);
        recWithIDIncremented.put("text", "textStringWithID_Inc");
        transactionManager.executeInSingleTrasaction(t -> {
            // new record
            EntityRecord r = persistenceEntityManager.createNewEntityRecord(recWithIDIncremented, t);
            assertEquals("textStringWithID_Inc", r.get("text"));
            assertEquals(Long.valueOf(778), r.get("_id"));
        });


    }


    @Test(expected = IdFieldException.class)
    public void n2_TestIdRequiredForModifyActions() throws GeminiException {
        EntityRecord newrec = new EntityRecord(dataTypeEntity);
        transactionManager.executeInSingleTrasaction(t -> {
            newrec.put("numberLong", 88);
            persistenceEntityManager.updateEntityRecordByID(newrec, t);
        });
    }

    @Test
    public void n3_TestSimpleReferenceType() throws GeminiException {
        EntityRecord savedEntity =
                transactionManager.executeInSingleTrasaction(t -> {
                    EntityRecord domain = new EntityRecord(domainEntity);
                    domain.put("code", "D1");
                    persistenceEntityManager.createNewEntityRecord(domain, t);

                    EntityRecord target = new EntityRecord(dataTypeEntity);
                    target.put("text", "textString");
                    target.put("domain1", "D1");

                    // get new saved entity record
                    EntityRecord savedTarget = persistenceEntityManager.createNewEntityRecord(target, t);
                    EntityReferenceRecord domain1 = savedTarget.get("domain1");
                    assertTrue(domain1.hasPrimaryKey());
                    assertEquals("D1", domain1.getLogicalKeyRecord().get("code"));
                    return savedTarget;
                });

        // update the entity withGeminiSearchString another FK
        transactionManager.executeInSingleTrasaction(t -> {
            EntityRecord domain = new EntityRecord(domainEntity);
            domain.put("code", "D2");
            EntityRecord savedDomain = persistenceEntityManager.createNewEntityRecord(domain, t);

            savedEntity.put("domain1", "D2");
            EntityRecord updatedRecord = persistenceEntityManager.updateEntityRecordByID(savedEntity, t);
            EntityReferenceRecord domain1 = updatedRecord.get("domain1");
            assertTrue(domain1.hasPrimaryKey());
            assertEquals("D2", domain1.getLogicalKeyRecord().get("code"));
            return savedDomain;
        });

    }


    //@Test(expected = AssertionError.class)
    public void n4_TestExpectAssertionError() throws SQLException, GeminiException {
        // deleting the Domain D2... excpeting assertion errore when try to get a record withGeminiSearchString reference to the deleted domain
        transactionManager.executeInSingleTrasaction(t -> {
            EntityRecord domain2 = new EntityRecord(domainEntity);
            domain2.put("code", "D2");
            Optional<EntityRecord> domain2OnDb = persistenceEntityManager.getEntityRecordByLogicalKey(domain2, t);
            assertTrue(domain2OnDb.isPresent());

            persistenceEntityManager.deleteEntityRecordByID(domain2OnDb.get(), t);

            EntityRecord targetEntity = new EntityRecord(dataTypeEntity);
            targetEntity.put("text", "textString");
            targetEntity.put("domain1", "D2");

            // db inconsistency - TODO
            persistenceEntityManager.getEntityRecordByLogicalKey(targetEntity, t);
        });
    }
}
