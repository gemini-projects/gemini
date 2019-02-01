package it.at7.gemini.performanceTest;

import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.GeminiTestBase;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.core.VoidTransactionCallback;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class PerformanceEntityManagerTest extends GeminiTestBase {
    static final Logger logger = LoggerFactory.getLogger(PerformanceEntityManagerTest.class);

    public static void main(String[] args) throws GeminiException {
        initializeTest();
        Instant start = Instant.now();
        executeInSingleTransaction();
        Instant finish = Instant.now();
        after();
        String elapsed = Duration.between(start, finish)
                .toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
        logger.info("ELAPSED Time: {} ", elapsed);
    }


    public static void executeInSingleTransaction() throws GeminiException {
        Entity dataTypeEntity = schemaManager.getEntity("TESTDATATYPE");
        transactionManager.executeInSingleTrasaction(new VoidTransactionCallback() {
            @Override
            public void doInTransaction(Transaction t) throws GeminiException {
                EntityRecord newrec = new EntityRecord(dataTypeEntity);
                newrec.put("numberDec", 77);
                newrec.put("numberFlt", 77.7);
                newrec.put("bool", false);
                for (int i = 0; i < 10000; i++) {
                    newrec.put("text", "textString" + i);
                    persistenceEntityManager.saveNewEntityRecord(newrec, t);
                }
            }
        });
    }

    public static void executeInMultipleTransaction() throws GeminiException {
        Entity dataTypeEntity = schemaManager.getEntity("TESTDATATYPE");
        EntityRecord newrec = new EntityRecord(dataTypeEntity);
        newrec.put("numberDec", 77);
        newrec.put("numberFlt", 77.7);
        newrec.put("bool", false);
        for (int i = 0; i < 10000; i++) {
            newrec.put("text", "otherTextString" + i);
            transactionManager.executeInSingleTrasaction(new VoidTransactionCallback() {
                @Override
                public void doInTransaction(Transaction t) throws GeminiException {
                    persistenceEntityManager.saveNewEntityRecord(newrec, t);
                }

            });
        }
    }
}
