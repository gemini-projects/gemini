package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Services;
import it.at7.gemini.exceptions.EntityException;
import it.at7.gemini.schema.Entity;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClosedDomainEntityManagerAbstTest {

    @Test(expected = EntityException.class)
    public void n1_craatetIsNotAllowed() throws Exception {
        EntityManager entityManager = Services.getEntityManager();
        entityManager.putIfAbsent(getEntityRecord());
    }

    @Test(expected = EntityException.class)
    public void n2_updateIsNotAllowed() throws Exception {
        EntityManager entityManager = Services.getEntityManager();
        entityManager.update(getEntityRecord());
    }

    @Test(expected = EntityException.class)
    public void n3_deleteIsNotAllowed() throws Exception {
        EntityManager entityManager = Services.getEntityManager();
        entityManager.delete(getEntityRecord());
    }

    private EntityRecord getEntityRecord() {
        Entity closed_domain = Services.getEntityManager().getEntity("Closed_Domain");
        EntityRecord er = new EntityRecord(closed_domain);
        er.put("code", "some_lk");
        return er;
    }

}
