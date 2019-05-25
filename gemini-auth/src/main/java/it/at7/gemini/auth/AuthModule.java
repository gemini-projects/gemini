package it.at7.gemini.auth;

import it.at7.gemini.conf.State;
import it.at7.gemini.core.Module;
import it.at7.gemini.core.*;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static it.at7.gemini.conf.State.SCHEMA_STORAGE_INITIALIZED;

@Service
@ComponentScan({"it.at7.gemini.auth"})
@ModuleDescription(
        name = "AUTH",
        dependencies = {"CORE"},
        order = -607)
public class AuthModule implements Module {
    private static final Logger logger = LoggerFactory.getLogger(AuthModule.class);

    private SchemaManager schemaManager;
    private TransactionManager transactionManager;
    private PersistenceEntityManager persistenceEntityManager;

    @Autowired
    public AuthModule(SchemaManager schemaManager, TransactionManager transactionManager, PersistenceEntityManager persistenceEntityManager) {
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.persistenceEntityManager = persistenceEntityManager;
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (actual == SCHEMA_STORAGE_INITIALIZED) {
            createPredefinedUserForEachModule(transaction);
        }
    }

    private void createPredefinedUserForEachModule(Optional<Transaction> transaction) throws GeminiException {
        logger.info("Check/Create predefined Users");
        assert transaction.isPresent();

        // GEMINI Core User
        Entity userEntity = schemaManager.getEntity(UserRef.NAME);
        EntityRecord entityRecord = new EntityRecord(userEntity);
        String username = AuthModuleRef.USERS.GEMINI;
        String description = "Auto generated user for " + username;
        entityRecord.put(UserRef.FIELDS.USERNAME, username);
        entityRecord.put(UserRef.FIELDS.DESCRIPTION, description);
        entityRecord.put(UserRef.FIELDS.FRAMEWORK, true);
        persistenceEntityManager.createOrUpdateEntityRecord(entityRecord, transaction.get());
    }
}
