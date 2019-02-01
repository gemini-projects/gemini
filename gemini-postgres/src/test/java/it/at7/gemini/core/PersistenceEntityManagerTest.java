package it.at7.gemini.core;

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
public class PersistenceEntityManagerTest extends GeminiTestBase {

    Entity dataTypeEntity = schemaManager.getEntity("TESTDATATYPE");
    Entity domainEntity = schemaManager.getEntity("TESTDOMAIN1");

    @Test
    public void n1_TestBasicTypesAndDuplicates() throws SQLException, GeminiException {
        assertNotNull(dataTypeEntity);

        EntityRecord newrec = new EntityRecord(dataTypeEntity);
        newrec.put("text", "textString");
        newrec.put("numberLong", 77);
        newrec.put("numberDouble", 77.7);
        newrec.put("bool", false);

        // save the new entity
        EntityRecord savedEntity =
                transactionManager.executeInSingleTrasaction(t -> {
                    // new record
                    EntityRecord entity = persistenceEntityManager.saveNewEntityRecord(newrec, t);
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
                persistenceEntityManager.saveNewEntityRecord(newrec, t);
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
            EntityRecord newSavedEntity = persistenceEntityManager.updateEntityRecord(savedEntity, t);
            assertEquals("textUpdatedKey", newSavedEntity.get("text"));
            assertEquals(Long.valueOf(88), newSavedEntity.get("numberLong"));
            assertEquals(Double.valueOf(88.88), newSavedEntity.get("numberDouble"));
            assertEquals(true, newSavedEntity.get("bool"));
            Optional<EntityRecord> updated = persistenceEntityManager.getRecordByLogicalKey(savedEntity, t);
            assertTrue(updated.isPresent()); // and only one
        });


        // first record (lk) no longer exists - we have updated also the key
        transactionManager.executeInSingleTrasaction(t -> {
            Optional<EntityRecord> recordByLogicalKey = persistenceEntityManager.getRecordByLogicalKey(newrec, t);
            assertFalse(recordByLogicalKey.isPresent());
        });


        // delete the record
        transactionManager.executeInSingleTrasaction(t -> {
            persistenceEntityManager.deleteEntity(savedEntity, t);
            Optional<EntityRecord> deletedRecord = persistenceEntityManager.getRecordByLogicalKey(newrec, t);
            assertFalse(deletedRecord.isPresent());
        });
    }


    @Test(expected = IdFieldException.class)
    public void n2_TestIdRequiredForModifyActions() throws SQLException, GeminiException {
        EntityRecord newrec = new EntityRecord(dataTypeEntity);
        transactionManager.executeInSingleTrasaction(t -> {
            newrec.put("numberLong", 88);
            persistenceEntityManager.updateEntityRecord(newrec, t);
        });
    }

    @Test
    public void n3_TestSimpleReferenceType() throws SQLException, GeminiException {
        EntityRecord savedEntity =
                transactionManager.executeInSingleTrasaction(t -> {
                    EntityRecord domain = new EntityRecord(domainEntity);
                    domain.put("code", "D1");
                    persistenceEntityManager.saveNewEntityRecord(domain, t);

                    EntityRecord target = new EntityRecord(dataTypeEntity);
                    target.put("text", "textString");
                    target.put("domain1", "D1");

                    // get new saved entity record
                    EntityRecord savedTarget = persistenceEntityManager.saveNewEntityRecord(target, t);
                    EntityReferenceRecord domain1 = savedTarget.get("domain1");
                    assertTrue(domain1.hasPrimaryKey());
                    assertEquals("D1", domain1.getLogicalKeyRecord().get("code"));
                    return savedTarget;
                });

        // update the entity with another FK
        transactionManager.executeInSingleTrasaction(t -> {
            EntityRecord domain = new EntityRecord(domainEntity);
            domain.put("code", "D2");
            EntityRecord savedDomain = persistenceEntityManager.saveNewEntityRecord(domain, t);

            savedEntity.put("domain1", "D2");
            EntityRecord updatedRecord = persistenceEntityManager.updateEntityRecord(savedEntity, t);
            EntityReferenceRecord domain1 = updatedRecord.get("domain1");
            assertTrue(domain1.hasPrimaryKey());
            assertEquals("D2", domain1.getLogicalKeyRecord().get("code"));
            return savedDomain;
        });

    }


    //@Test(expected = AssertionError.class)
    public void n4_TestExpectAssertionError() throws SQLException, GeminiException {
        // deleting the Domain D2... excpeting assertion errore when try to get a record with reference to the deleted domain
        transactionManager.executeInSingleTrasaction(t -> {
            EntityRecord domain2 = new EntityRecord(domainEntity);
            domain2.put("code", "D2");
            Optional<EntityRecord> domain2OnDb = persistenceEntityManager.getRecordByLogicalKey(domain2, t);
            assertTrue(domain2OnDb.isPresent());

            persistenceEntityManager.deleteEntity(domain2OnDb.get(), t);

            EntityRecord targetEntity = new EntityRecord(dataTypeEntity);
            targetEntity.put("text", "textString");
            targetEntity.put("domain1", "D2");

            // db inconsistency - TODO
            persistenceEntityManager.getRecordByLogicalKey(targetEntity, t);
        });
    }
}
