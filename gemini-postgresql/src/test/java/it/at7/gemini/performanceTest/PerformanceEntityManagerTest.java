package it.at7.gemini.performanceTest;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.core.VoidTransactionCallback;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;

public class PerformanceEntityManagerTest extends GeminiPostgresqlTestBase {
    static final Logger logger = LoggerFactory.getLogger(PerformanceEntityManagerTest.class);

    static class MainSingleTransaction {
        public static void main(String[] args) throws GeminiException {
            initializeTest();
            Instant start = Instant.now();
            executeInSingleTransaction(100000);
            Instant finish = Instant.now();
            after();
            String elapsed = Duration.between(start, finish)
                    .toString()
                    .substring(2)
                    .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                    .toLowerCase();
            logger.info("ELAPSED Time: {} ", elapsed);
        }
    }

    static class MainMultipleTransaction {
        public static void main(String[] args) throws GeminiException {
            initializeTest();
            Instant start = Instant.now();
            executeInMultipleTransaction(100000);
            Instant finish = Instant.now();
            after();
            String elapsed = Duration.between(start, finish)
                    .toString()
                    .substring(2)
                    .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                    .toLowerCase();
            logger.info("ELAPSED Time: {} ", elapsed);
        }
    }

    public static void executeInSingleTransaction(long howMany) throws GeminiException {
        Entity dataTypeEntity = schemaManager.getEntity("TESTDATATYPE");
        transactionManager.executeInSingleTrasaction(new VoidTransactionCallback() {
            @Override
            public void doInTransaction(Transaction t) throws GeminiException {
                EntityRecord newrec = new EntityRecord(dataTypeEntity);
                newrec.put("numberLong", 10);
                newrec.put("numberDouble", 11.1);
                newrec.put("long", 10);
                newrec.put("double", 11.1);
                newrec.put("bool", true);
                newrec.put("date", LocalDate.of(1989, 6, 9));
                newrec.put("time", LocalTime.of(7, 7, 7));
                newrec.put("datetime", LocalDateTime.of(1989, 6, 9, 7, 7, 7));
                newrec.put("textarray", new String[]{"abc", "def"});
                for (int i = 0; i < howMany; i++) {
                    newrec.put("text", "textString" + i);
                    persistenceEntityManager.createNewEntityRecord(newrec, t);
                }
            }
        });
    }

    public static void executeInMultipleTransaction(long howMany) throws GeminiException {
        Entity dataTypeEntity = schemaManager.getEntity("TESTDATATYPE");
        EntityRecord newrec = new EntityRecord(dataTypeEntity);
        newrec.put("numberLong", 10);
        newrec.put("numberDouble", 11.1);
        newrec.put("long", 10);
        newrec.put("double", 11.1);
        newrec.put("bool", true);
        newrec.put("date", LocalDate.of(1989, 6, 9));
        newrec.put("time", LocalTime.of(7, 7, 7));
        newrec.put("datetime", LocalDateTime.of(1989, 6, 9, 7, 7, 7));
        newrec.put("textarray", new String[]{"abc", "def"});
        for (int i = 0; i < howMany; i++) {
            newrec.put("text", "otherTextString" + i);
            transactionManager.executeInSingleTrasaction(new VoidTransactionCallback() {
                @Override
                public void doInTransaction(Transaction t) throws GeminiException {
                    persistenceEntityManager.createNewEntityRecord(newrec, t);
                }
            });
        }
    }
}
