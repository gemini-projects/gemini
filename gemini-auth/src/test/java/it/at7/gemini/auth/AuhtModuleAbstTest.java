package it.at7.gemini.auth;

import it.at7.gemini.UnitTestNoMockWeb;
import it.at7.gemini.auth.core.AuthModuleRef;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.FilterContext;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AuhtModuleAbstTest extends UnitTestNoMockWeb {

    @Test
    public void n1_testAuthModuleInitialization() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        Entity entity = entityManager.getEntity(UserRef.NAME);
        List<EntityRecord> records = entityManager.getRecordsMatching(entity, FilterContext.withGeminiSearchString(UserRef.FIELDS.USERNAME + " == " + AuthModuleRef.USERS.GEMINI));
        Assert.assertEquals(1, records.size());
        EntityRecord geminiUserRec = records.get(0);
        Assert.assertNotNull(geminiUserRec);
    }
}
