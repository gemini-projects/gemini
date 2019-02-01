package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import org.junit.Assert;
import org.junit.Test;

public abstract class TransactionManagerImplTest {

    @Test
    public void testMultipleTransaction() throws GeminiException {
        TransactionManager transactionManager = Services.getTransactionManager();
        Transaction t1 = transactionManager.openTransaction();
        Transaction t2 = transactionManager.openTransaction();
        Assert.assertFalse(t1.equals(t2));
    }

}