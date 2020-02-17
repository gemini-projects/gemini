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
    private final EntityCacheManager entityCacheManager;


    @Autowired
    public TransactionManagerImpl(ApplicationContext applicationContext, EntityCacheManager entityCacheManager) {
        this.applicationContext = applicationContext;
        this.entityCacheManager = entityCacheManager;
    }

    @Override
    public Transaction openRawTransaction() throws GeminiException {
        Transaction transaction = applicationContext.getBean(Transaction.class);
        transaction.open();
        return transaction;
    }

    @Override
    public <T> T executeEntityManagedTransaction(TransactionCallback callback) throws GeminiException {
        try (Transaction transaction = openRawTransaction()) {
            Object o = callback.doInTransaction(transaction);
            this.entityCacheManager.notifyUpdate(transaction.getUpdatedEntities(), transaction);
            transaction.commit();
            return (T) o;
        }
    }

    @Override
    public void executeEntityManagedTransaction(VoidTransactionCallback callback) throws GeminiException {
        try (Transaction transaction = openRawTransaction()) {
            callback.doInTransaction(transaction);
            this.entityCacheManager.notifyUpdate(transaction.getUpdatedEntities(), transaction);
            transaction.commit();
        }
    }
}
