package it.at7.gemini.dsl;

import it.at7.gemini.dsl.entities.RawEntity;
import it.at7.gemini.dsl.entities.RawSchema;
import org.junit.Test;

import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

public class SchemaParserTest {

    @Test
    public void testEntityParser() throws SyntaxError {
        String dsl = "ENTITY User {" +
                "   TEXT    code *" +
                " NUMBER    value" +
                " }" +
                " " +
                " ENTITY Test {" +
                " }";
        StringReader reader = new StringReader(dsl);
        RawSchema rawSchema = SchemaParser.parse(reader);
        Set<RawEntity> rawEntities = rawSchema.getRawEntities();
        assertEquals(2, rawEntities.size());

        Map<String, RawEntity> rawModelsByName = rawSchema.getRawEntitiesByName();
        RawEntity user = rawModelsByName.get("USER");
        Optional<RawEntity.Entry> code = user.getEntries().stream().filter(RawEntity.Entry::isLogicalKey).findAny();
        assertTrue(code.isPresent());
        RawEntity.Entry entry = code.get();
        assertEquals("code", entry.getName());
    }

    @Test
    public void testEntityImplementsInterfaceParse() throws SyntaxError {
        String dsl = "INTERFACE User {" +
                "   TEXT    code *" +
                " NUMBER    value" +
                " }" +
                " " +
                " ENTITY Test IMPLEMENTS User {" +
                "   TEXT test *" +
                " }";
        StringReader reader = new StringReader(dsl);
        RawSchema rawSchema = SchemaParser.parse(reader);
        Set<RawEntity> rawEntities = rawSchema.getRawEntities();
        assertEquals(1, rawEntities.size());

        Set<RawEntity> rawEntityInterfaces = rawSchema.getRawEntityInterfaces();
        assertEquals(1, rawEntityInterfaces.size());

        Map<String, RawEntity> rawModelsByName = rawSchema.getRawEntitiesByName();
        RawEntity user = rawModelsByName.get("TEST");
        Optional<RawEntity.Entry> test = user.getEntries().stream().filter(RawEntity.Entry::isLogicalKey).findAny();
        assertTrue(test.isPresent());
        RawEntity.Entry entry = test.get();
        assertEquals("test", entry.getName());
    }


    @Test
    public void testEmbedableEntityParse() throws SyntaxError {
        String dsl = "ENTITY EMBEDABLE Embedable {" +
                "   TEXT    code " +
                " NUMBER    value" +
                " }" +
                " " +
                " ENTITY TestEmb  {" +
                "   TEXT test *" +
                "   Embedable embeded" +
                " }";
        StringReader reader = new StringReader(dsl);
        RawSchema rawSchema = SchemaParser.parse(reader);
        Set<RawEntity> rawEntities = rawSchema.getRawEntities();
        assertEquals(2, rawEntities.size());

        Map<String, RawEntity> rawEntitisByName = rawSchema.getRawEntitiesByName();
        RawEntity embedable = rawEntitisByName.get("EMBEDABLE");
        assertTrue(embedable.isEmbedable());
    }

    @Test(expected = SyntaxError.class)
    public void testSyntaxError() throws SyntaxError {
        String dsl = "ENTITY Error AHHH";
        SchemaParser.parse(new StringReader(dsl));
    }

}