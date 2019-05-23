package it.at7.gemini.core.entitymanager;

import it.at7.gemini.UnitTestNoMockWeb;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.core.type.Password;
import it.at7.gemini.exceptions.GeminiException;
import org.junit.Assert;
import org.junit.Test;

public abstract class PasswordEntityManagerAbstTest extends UnitTestNoMockWeb {

    @Test
    public void n1_putIfAbsent() throws GeminiException {
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey");
        entityRecord.put("password", "password");
        Password password = entityRecord.get("password");
        EntityManager entityManager = Services.getEntityManager();
        EntityRecord persistedER = entityManager.putIfAbsent(entityRecord);
        Assert.assertEquals(password, persistedER.get("password"));
    }
}
