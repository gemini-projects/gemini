package it.at7.gemini.dsl;

import it.at7.gemini.dsl.entities.SchemaRawRecords;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

public class RecordParserTest {

    @Test
    public void testRecordParser() throws SyntaxError, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("records.atr");
        Map<String, SchemaRawRecords> recordByEntity = RecordParser.parse(new InputStreamReader(resourceAsStream));

        SchemaRawRecords user = recordByEntity.get("USER");
        Assert.assertEquals( "USER", user.getEntity());
        List<Object> records = user.getRecords();
        Assert.assertEquals(1, records.size());
        Map<String, Object> record = (Map<String, Object>) records.get(0);
        Assert.assertEquals(1, record.get("a"));
        Assert.assertEquals("st", record.get("b"));
        Assert.assertTrue(Map.class.isAssignableFrom(record.get("map").getClass()));
        Assert.assertEquals(1, ((Map<String, Object>) record.get("map")).get("a"));

        SchemaRawRecords array = recordByEntity.get("ARRAY");
        List<Object> arrayRecords = array.getRecords();
        Assert.assertEquals(2, arrayRecords.size());
    }

}