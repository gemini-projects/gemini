package it.at7.gemini.core;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityRef;
import it.at7.gemini.schema.FieldRef;
import it.at7.gemini.schema.FieldResolutionDef;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SchemaManagerAbstTest {

    private static SchemaManager schemaManager;
    private static TransactionManager transactionManager;
    private static PersistenceEntityManager persistenceEntityManager;

    @Test
    public void n1_TestSchemaInitializationWithoutError() {
        schemaManager = Services.getSchemaManager();
        transactionManager = Services.getTransactionManager();
        persistenceEntityManager = Services.getPersistenceEntityManager();

        StateManager stateManager = Services.getStateManager();
        State actualState = stateManager.getActualState();
        Assert.assertEquals(State.INITIALIZED, actualState);
    }

    @Test
    public void n2_TestCoreCommonEntities() throws SQLException, GeminiException {
        // first check that we have entities..
        Entity entityEntity = schemaManager.getEntity(EntityRef.NAME); // the entity of the entity
        assertNotNull(entityEntity);
        Entity fieldEntity = schemaManager.getEntity(FieldRef.NAME);
        assertNotNull(fieldEntity);
        Entity fieldResolutionEntity = schemaManager.getEntity(FieldResolutionDef.NAME);
        assertNotNull(fieldResolutionEntity);

        // then check some default records that must be in db
        transactionManager.executeInSingleTrasaction(t -> {
            Map<String, Object> entityLogicalKey = Map.of(EntityRef.FIELDS.NAME, "ENTITY");
            EntityRecord entityRecordWithLogicalKey = RecordConverters.entityRecordFromMap(entityEntity, entityLogicalKey);
            Optional<EntityRecord> recordByLogicalKey = persistenceEntityManager.getEntityRecordByLogicalKey(entityEntity, entityRecordWithLogicalKey, t);
            assertTrue(recordByLogicalKey.isPresent());
            EntityRecord entityRecord = recordByLogicalKey.get();
            assertEquals("ENTITY", entityRecord.get(EntityRef.FIELDS.NAME));

            Map<String, Object> nameFieldLogicalKey = Map.of(
                    FieldRef.FIELDS.NAME, "name",
                    FieldRef.FIELDS.ENTITY, "ENTITY");
            EntityRecord lkNameFieldRecord = RecordConverters.entityRecordFromMap(fieldEntity, nameFieldLogicalKey);
            Optional<EntityRecord> fieldRecordByLogicalKey = persistenceEntityManager.getEntityRecordByLogicalKey(fieldEntity, lkNameFieldRecord, t);
            assertTrue(fieldRecordByLogicalKey.isPresent());
            EntityRecord nameFieldRecord = fieldRecordByLogicalKey.get();
            assertEquals("name", nameFieldRecord.get(FieldRef.FIELDS.NAME));
            EntityReferenceRecord refRecord = nameFieldRecord.get(FieldRef.FIELDS.ENTITY); // reference to entity
            DynamicRecord logicalKeyValue = refRecord.getLogicalKeyRecord();
            assertEquals("ENTITY", logicalKeyValue.get(EntityRef.FIELDS.NAME));
            assertEquals(true, nameFieldRecord.get(FieldRef.FIELDS.ISLOGICALKEY));
            return true;
        });
    }
}

