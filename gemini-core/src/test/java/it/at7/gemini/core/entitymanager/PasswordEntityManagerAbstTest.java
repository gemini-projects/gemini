package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.type.Password;
import it.at7.gemini.exceptions.EntityRecordException;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PasswordEntityManagerAbstTest {

    @Test
    public void n1_putIfAbsent() throws GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-PWDTest");
        entityRecord.put("password", "password");
        Password password = entityRecord.get("password");
        EntityManager entityManager = Services.getEntityManager();
        EntityRecord persistedER = entityManager.putIfAbsent(entityRecord);
        Assert.assertEquals(password, persistedER.get("password"));
    }

    @Test(expected = EntityRecordException.class)
    public void n2_putIfAbsentMultipleLK() throws GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-PWDTest");
        EntityManager entityManager = Services.getEntityManager();
        entityManager.putIfAbsent(entityRecord);
    }

    @Test
    public void n3_updateExistentRecordWithPWD() throws GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-PWDTest");
        entityRecord.put("password", "otherPassword");
        Password password = new Password("otherPassword");
        EntityManager entityManager = Services.getEntityManager();
        EntityRecord persistedER = entityManager.putOrUpdate(entityRecord);
        Assert.assertEquals(password, persistedER.get("password"));
    }
}
