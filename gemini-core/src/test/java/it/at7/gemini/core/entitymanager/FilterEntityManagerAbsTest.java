package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Optional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class FilterEntityManagerAbsTest {

    @Test
    public void n1_testSingleStringFilter() throws GeminiException {
        EntityManager entityManager = Services.getEntityManager();
        Entity fieldEntity = entityManager.getEntity("FIELD");

        // single search
        FilterContext filterContext = FilterContext.withGeminiSearchString("name == name");// all fields with name name
        List<EntityRecord> nameFields = entityManager.getRecordsMatching(fieldEntity, filterContext);
        Assert.assertTrue(!nameFields.isEmpty());
        for (EntityRecord nameField : nameFields) {
            String name = nameField.get("name");
            Assert.assertEquals("name", name);
        }

        // OR search
        filterContext = FilterContext.withGeminiSearchString("name == name or name == module"); // all fields with name = name or module
        List<EntityRecord> nameOrCodeFields = entityManager.getRecordsMatching(fieldEntity, filterContext);
        Assert.assertTrue(!nameOrCodeFields.isEmpty());
        Assert.assertTrue(nameOrCodeFields.size() > nameFields.size());
        Optional<EntityRecord> name = nameOrCodeFields.stream().filter(f -> f.get("name").equals("name")).findFirst();
        Assert.assertTrue(name.isPresent());
        Optional<EntityRecord> module = nameOrCodeFields.stream().filter(f -> f.get("name").equals("module")).findFirst();
        Assert.assertTrue(module.isPresent());

        // AND search with Long and String (both basic types on
        EntityRecord entityRecord = TestData.getTestDataTypeEntityRecord("logKey-basicType");
        entityRecord.put("numberLong", 10);
        entityManager.putIfAbsent(entityRecord);
        filterContext = FilterContext.withGeminiSearchString("text == logKey-basicType and numberLong == 10"); // all fields with name name
        List<EntityRecord> andMatching = entityManager.getRecordsMatching(TestData.getTestDataTypeEntity(), filterContext);
        Assert.assertEquals(1, andMatching.size());

        // IN operator (same test as or)
        filterContext = FilterContext.withGeminiSearchString("name=in=(name, module)");
        nameOrCodeFields = entityManager.getRecordsMatching(fieldEntity, filterContext);
        Assert.assertTrue(!nameOrCodeFields.isEmpty());
        Assert.assertTrue(nameOrCodeFields.size() > nameFields.size());
        name = nameOrCodeFields.stream().filter(f -> f.get("name").equals("name")).findFirst();
        Assert.assertTrue(name.isPresent());
        module = nameOrCodeFields.stream().filter(f -> f.get("name").equals("module")).findFirst();
        Assert.assertTrue(module.isPresent());

        // todo other operators ??
    }

}
