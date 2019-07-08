package it.at7.gemini.auth;

import it.at7.gemini.UnitTestNoMockWeb;
import it.at7.gemini.auth.core.AuthModuleRef;
import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.EntityRef;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AuthMetaEventsAbstTest extends UnitTestNoMockWeb {

    @Test
    public void n1_testAuthEventBefore() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        List<EntityRecord> records = entityManager.getRecordsMatching(entityManager.getEntity(EntityRef.NAME), FilterContext.withGeminiSearchString("name == ENTITY"));
        Assert.assertEquals(1, records.size());
        EntityRecord record = records.get(0);
        EntityReferenceRecord createdUserRec = record.get("created_user");
        EntityReferenceRecord modifiedUserRec = record.get("modified_user");
        Assert.assertEquals(AuthModuleRef.USERS.GEMINI, createdUserRec.getLogicalKeyRecord().get("username"));
        Assert.assertEquals(AuthModuleRef.USERS.GEMINI, modifiedUserRec.getLogicalKeyRecord().get("username"));
    }

}