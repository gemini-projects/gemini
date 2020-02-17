package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import org.junit.Assert;
import org.junit.Test;

public class TransactionManagerAbstTest {

    @Test
    public void testMultipleTransaction() throws GeminiException {
        TransactionManager transactionManager = Services.getTransactionManager();
        Transaction t1 = transactionManager.openRawTransaction();
        Transaction t2 = transactionManager.openRawTransaction();
        Assert.assertNotEquals(t1, t2);
    }

}