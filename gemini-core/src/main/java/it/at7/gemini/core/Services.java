package it.at7.gemini.core;

import it.at7.gemini.core.persistence.PersistenceEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Singleton to get all services in a static way
 */
@Service
public class Services {
    private static EntityManager entityManager;
    private static PersistenceEntityManager persistenceEntityManager;
    private static SchemaManager schemaManager;
    private static StateManager stateManager;
    private static TransactionManager transactionManager;
    private static GeminiConfigurationService configurationService;

    @Autowired
    public Services(EntityManager entityManager,
                    PersistenceEntityManager persistenceEntityManager,
                    SchemaManager schemaManager, StateManager stateManager,
                    TransactionManager transactionManager,
                    GeminiConfigurationService configurationService
    ) {
        Services.entityManager = entityManager;
        Services.persistenceEntityManager = persistenceEntityManager;
        Services.schemaManager = schemaManager;
        Services.stateManager = stateManager;
        Services.transactionManager = transactionManager;
        Services.configurationService = configurationService;
    }

    public static EntityManager getEntityManager() {
        assert entityManager != null;
        return entityManager;
    }

    public static PersistenceEntityManager getPersistenceEntityManager() {
        assert persistenceEntityManager != null;
        return persistenceEntityManager;
    }

    public static SchemaManager getSchemaManager() {
        assert schemaManager != null;
        return schemaManager;
    }


    public static StateManager getStateManager() {
        assert stateManager != null;
        return stateManager;
    }

    public static TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public static GeminiConfigurationService getConfigurationService() {
        assert configurationService != null;
        return configurationService;
    }
}
