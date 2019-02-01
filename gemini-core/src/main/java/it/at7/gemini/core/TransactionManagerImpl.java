package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class TransactionManagerImpl implements TransactionManager {
    private final static Logger logger = LoggerFactory.getLogger(TransactionManagerImpl.class);
    private final ApplicationContext applicationContext;


    @Autowired
    public TransactionManagerImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Transaction openTransaction() throws GeminiException {
        Transaction transaction = applicationContext.getBean(Transaction.class);
        transaction.open();
        return transaction;
    }


    @Override
    public <T> T executeInSingleTrasaction(TransactionCallback callback) throws GeminiException {
        try (Transaction transaction = openTransaction()) {
            Object o = callback.doInTransaction(transaction);
            transaction.commit();
            return (T) o;
        }
    }
    @Override
    public void executeInSingleTrasaction(VoidTransactionCallback callback) throws GeminiException {
        try (Transaction transaction = openTransaction()) {
            callback.doInTransaction(transaction);
            transaction.commit();
        }
    }
}
