package it.at7.gemini.auth.events;

import it.at7.gemini.api.ApiListenerManagerInit;
import it.at7.gemini.api.RestAPIControllerListener;
import it.at7.gemini.auth.AuthModule;
import it.at7.gemini.auth.core.AuthEntityOperationContext;
import it.at7.gemini.auth.core.AuthModuleRef;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.core.*;
import it.at7.gemini.core.events.BeforeInsertField;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.core.events.OnUpdateField;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Events(entityName = Entity.CORE_META, order = -100)
public class AuthMetaEvents implements SchemaManagerInitListener, RestAPIControllerListener {

    private final AuthModule authModule;
    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;
    private final ApiListenerManagerInit apiListenersManager;


    Map<String, EntityReferenceRecord> entityRefUserCache = new HashMap<>();

    @Autowired
    public AuthMetaEvents(ApplicationContext applicationContext, EntityManager entityManager, ApiListenerManagerImpl apiListenersManager) {
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
        this.authModule = applicationContext.getBean(AuthModule.class);
        this.apiListenersManager = apiListenersManager;
        this.apiListenersManager.registerApiControllerListener(this);
    }

    @BeforeInsertField(field = "created_user")
    @BeforeInsertField(field = "modified_user")
    @OnUpdateField(field = "modified_user")
    public Object createdUser(EventContext eventContext) throws GeminiException {
        Optional<EntityOperationContext> entityOperationContext = eventContext.getEntityOperationContext();
        if (entityOperationContext.isPresent()) {
            EntityOperationContext eop = entityOperationContext.get();
            Optional<AuthEntityOperationContext> authEntityOpContext = eop.getModuleEntityOpContext(authModule);
            if (authEntityOpContext.isPresent()) {
                AuthEntityOperationContext aop = authEntityOpContext.get();

                return getUserEntityReferenceRec(aop.getUsername(), eventContext);

            }
        }
        return null;
    }

    private Object getUserEntityReferenceRec(String username, EventContext eventContext) throws GeminiException {
        EntityReferenceRecord userRef = entityRefUserCache.get(username);
        if (userRef == null) {
            Optional<Transaction> transaction = eventContext.getTransaction();
            if (transaction.isPresent()) {
                Entity userEntity = entityManager.getEntity(UserRef.NAME);
                List<EntityRecord> users = entityManager.getRecordsMatching(userEntity, UserRef.FIELDS.USERNAME, username, transaction.get());
                assert !users.isEmpty();
                EntityRecord entityRecord = users.get(0);
                EntityReferenceRecord entityReferenceRecord = EntityReferenceRecord.fromPKValue(userEntity, entityRecord.getID());
                entityRefUserCache.put(username, entityReferenceRecord);
                return entityReferenceRecord;
            } else {
                return username;
            }
        }
        return userRef;
    }


    /**
     * Add the default user to CORE framework entity records (used on schema initialization)
     */
    @Override
    public void onSchemasEntityRecords(EntityOperationContext entityOperationContext) {
        /*
            WHEN application starts all the predefined entities are created by the Core USER Module
         */
        AuthEntityOperationContext opContext = new AuthEntityOperationContext(AuthModuleRef.USERS.GEMINI);
        entityOperationContext.putModuleEntityOpContext(authModule, opContext);
    }

    /**
     * Extract the logged request user and add it to the EntityOperation Context (to be used when insert/update)
     */
    @Override
    public void onEntityOperationContextCreate(HttpServletRequest request, EntityOperationContext entityOperationContext, String entity, Object body) {
        String remoteUser = request.getRemoteUser();
        AuthEntityOperationContext opContext = new AuthEntityOperationContext(remoteUser);
        entityOperationContext.putModuleEntityOpContext(authModule, opContext);
    }
}

