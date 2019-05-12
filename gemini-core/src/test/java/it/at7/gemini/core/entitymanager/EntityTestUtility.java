package it.at7.gemini.core.entitymanager;

import it.at7.gemini.core.EntityRecord;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class EntityTestUtility {

    public static void testDefaulValues(EntityRecord testEntity, String lk) {
        assertEquals(lk, testEntity.get("text")); // real field
        assertEquals(0, (long) testEntity.get("numberLong")); // default
        assertEquals(0, (long) testEntity.get("numberDouble")); // default
        assertEquals(0., testEntity.get("double"), 0.001); // default
        assertEquals(0, (long) testEntity.get("long")); // default
        assertEquals(false, testEntity.get("bool")); // default
        assertArrayEquals(new String[]{}, testEntity.get("textarray")); // default
        assertNull(testEntity.get("date")); // default
        assertNull(testEntity.get("time")); // default
        assertNull(testEntity.get("datetime")); // default
    }

    public static void testDefaultMetaValues(EntityRecord testEntity) {
        LocalDateTime created = testEntity.get("created");
        LocalDateTime modified = testEntity.get("modified");
        assertNotNull(created);
        assertNotNull(modified);
    }

    public static void checkMetaModifiedChanged(EntityRecord updated) {
        LocalDateTime created = updated.get("created");
        LocalDateTime modified = updated.get("modified");
        assertNotEquals(created, modified);
    }
}
