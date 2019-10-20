package it.at7.gemini.auth;

import it.at7.gemini.auth.core.AuthModuleRef;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static it.at7.gemini.conf.State.SCHEMA_EVENTS_LOADED;

@Service
@ModuleDescription(
        name = "AUTH",
        dependencies = {"CORE"},
        order = -607)
@ComponentScan("it.at7.gemini.auth.core")
@ComponentScan("it.at7.gemini.auth.events")
@ConditionalOnProperty(name = "gemini.auth", havingValue = "true", matchIfMissing = true)
public class AuthModule implements GeminiModule {
    private static final Logger logger = LoggerFactory.getLogger(AuthModule.class);

    private final SchemaManager schemaManager;
    private final TransactionManager transactionManager;
    private final EntityManager entityManager;
    private final ApplicationContext applicationContext;

    @Autowired
    public AuthModule(SchemaManager schemaManager,
                      TransactionManager transactionManager,
                      EntityManager entityManager,
                      ApplicationContext applicationContext
    ) {
        this.schemaManager = schemaManager;
        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onChange(State previous, State actual, Optional<Transaction> transaction) throws GeminiException {
        if (actual == SCHEMA_EVENTS_LOADED) {
            checkOrcreatePredefinedUsers(transaction);
        }
    }

    private void checkOrcreatePredefinedUsers(Optional<Transaction> transaction) throws GeminiException {
        logger.info("Check/Create predefined Users");
        assert transaction.isPresent();
        Transaction t = transaction.get();
        Entity userEntity = schemaManager.getEntity(UserRef.NAME);

        // GEMINI Core User
        String username = AuthModuleRef.USERS.GEMINI;
        EntityReferenceRecord entityReferenceRecord = FieldConverters.logicalKeyFromObject(userEntity, username);
        Optional<EntityRecord> userRec = entityManager.getOptional(userEntity, entityReferenceRecord, t);
        if (!userRec.isPresent()) {
            String description = "Auto generated user for " + username;
            EntityRecord geminiFrameworkUser = new EntityRecord(userEntity);
            geminiFrameworkUser.put(UserRef.FIELDS.USERNAME, username);
            geminiFrameworkUser.put(UserRef.FIELDS.DESCRIPTION, description);
            geminiFrameworkUser.put(UserRef.FIELDS.FRAMEWORK, true);
            entityManager.putIfAbsent(geminiFrameworkUser, t);
        }

        // Admin
        String adminUsername = AuthModuleRef.USERS.ADMINISTRATOR;
        entityReferenceRecord = FieldConverters.logicalKeyFromObject(userEntity, adminUsername);
        Optional<EntityRecord> adminRec = entityManager.getOptional(userEntity, entityReferenceRecord, t);
        if (!adminRec.isPresent()) {
            String adminiDescription = "Auto generated user for " + adminUsername;
            EntityRecord adminUer = new EntityRecord(userEntity);
            adminUer.put(UserRef.FIELDS.USERNAME, adminUsername);
            adminUer.put(UserRef.FIELDS.DESCRIPTION, adminiDescription);
            adminUer.put(UserRef.FIELDS.FRAMEWORK, false);
            adminUer.put(UserRef.FIELDS.PASSWORD, adminUsername);
            entityManager.putIfAbsent(adminUer, t);
        }
    }

}
