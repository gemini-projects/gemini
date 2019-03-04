package it.at7.gemini.dsl;

import it.at7.gemini.dsl.entities.EntityRawRecords;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class RecordParserTest {

    @Test
    public void testRecordParser() throws SyntaxError, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("records.atr");
        Map<String, EntityRawRecords> recordByEntity = RecordParser.parse(new InputStreamReader(resourceAsStream));

        EntityRawRecords user = recordByEntity.get("USER");
        Assert.assertEquals("USER", user.getEntity());

        Map<String, EntityRawRecords.VersionedRecords> versionedRecords = user.getVersionedRecords();
        EntityRawRecords.VersionedRecords initVersion = versionedRecords.get("VERSION");
        Assert.assertNotNull(initVersion);
        Assert.assertEquals(1, initVersion.getVersionProgressive());
        Assert.assertEquals(1, initVersion.getRecords().size());

        Map<String, Object> record = (Map<String, Object>) initVersion.getRecords().get(0);
        Assert.assertEquals(1, record.get("a"));
        Assert.assertEquals("st", record.get("b"));
        Assert.assertTrue(Map.class.isAssignableFrom(record.get("map").getClass()));
        Assert.assertEquals(1, ((Map<String, Object>) record.get("map")).get("a"));

        EntityRawRecords array = recordByEntity.get("ARRAY");
        Map<String, EntityRawRecords.VersionedRecords> arrayVersionedRecord = array.getVersionedRecords();
        EntityRawRecords.VersionedRecords v1 = arrayVersionedRecord.get("V");
        Assert.assertEquals(2, v1.getRecords().size()
        );
    }

}