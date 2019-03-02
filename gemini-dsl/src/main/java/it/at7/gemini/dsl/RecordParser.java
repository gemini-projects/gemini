package it.at7.gemini.dsl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.at7.gemini.dsl.entities.SchemaRawRecords;
import it.at7.gemini.dsl.entities.SchemaRawRecordBuilder;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static it.at7.gemini.dsl.RecordParser.Token.*;

public class RecordParser {
    private final LineNumberReader reader;
    private Scanner scanner;

    public RecordParser(Reader reader) {
        this.reader = new LineNumberReader(reader);
    }

    public Map<String, SchemaRawRecords> parse() throws SyntaxError, IOException {
        Map<String, SchemaRawRecordBuilder> builders = new HashMap<>();
        String line = reader.readLine();
        this.scanner = new Scanner(line);
        while (has(ENTITYRECORD)) {
            expect(ENTITYRECORD);
            String entityName = expect(ENTITYNAME);
            SchemaRawRecordBuilder schemaRawRecordBuilder = builders.computeIfAbsent(entityName.toUpperCase(), SchemaRawRecordBuilder::new);
            StringBuilder jsonBuilder = new StringBuilder();
            String versionNameOrDefault = expect(VERSIONNAME); // DEFAULT in case of default record
            long versionProgressive = 0; boolean def = true;
            if(!versionNameOrDefault.equals("DEFAULT")) {
                def = false;
                versionProgressive = Long.parseLong(expect(VERSIONPROGRESSIVE));
            }
            while ((line = reader.readLine()) != null) {
                this.scanner = new Scanner(line);
                if (!has(ENTITYRECORD)) {
                    jsonBuilder.append(line);
                } else {
                    break;
                }
            }
            String jsonString = jsonBuilder.toString();
            // convert JSON string to Map

            if (jsonString.charAt(0) == '{') {
                Object singleRecord = new ObjectMapper().readValue(jsonString,
                        new TypeReference<Map<String, Object>>() {
                        });
                if (def) schemaRawRecordBuilder.setDefaultRecord(singleRecord);
                else schemaRawRecordBuilder.addRecord(versionNameOrDefault, versionProgressive, singleRecord);
            } else {
                assert jsonString.charAt(0) == '[';
                List<Object> listRecord = new ObjectMapper().readValue(jsonString,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                assert !def; // array not allowed for DEFAULT
                schemaRawRecordBuilder.addRecords(versionNameOrDefault, versionProgressive, listRecord);
            }
        }
        return builders.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, b -> b.getValue().build()));
    }

    private boolean checkDefault() {
        return has(DEFAULT);
    }

    private String expect(Token token) throws SyntaxError {
        try {
            return scanner.next(token.pattern);
        } catch (InputMismatchException e) {
            String next = scanner.next();
            throw new SyntaxError(String.format("Line %d: Expected %s while found %s", reader.getLineNumber(), token.pattern, next));
        }
    }

    private boolean has(Token token) {
        return scanner.hasNext(token.pattern);
    }

    public static Map<String, SchemaRawRecords> parse(Reader r) throws SyntaxError, IOException {
        return new RecordParser(r).parse();
    }

    public enum Token {
        ENTITYRECORD("ENTITY-RECORD"),
        ENTITYNAME("[a-zA-z]+"),
        VERSIONNAME("[a-zA-z]+"),
        VERSIONPROGRESSIVE("[0-9]+"),
        DEFAULT("DEFAULT"),
        L_BRACE(Pattern.quote("{")),
        L_BRACKET(Pattern.quote("[")),
        R_BRACE(Pattern.quote("}"));

        private String pattern;

        Token(String pattern) {
            this.pattern = pattern;
        }
    }
}
