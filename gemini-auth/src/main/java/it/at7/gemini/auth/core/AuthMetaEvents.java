package it.at7.gemini.auth.core;

import it.at7.gemini.api.ApiListenerManagerInit;
import it.at7.gemini.api.RestAPIControllerListener;
import it.at7.gemini.core.ApiListenerManagerImpl;
import it.at7.gemini.core.EntityManager;
import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.SchemaManagerInitListener;
import it.at7.gemini.core.events.BeforeInsertField;
import it.at7.gemini.core.events.EventContext;
import it.at7.gemini.core.events.Events;
import it.at7.gemini.core.events.OnUpdateField;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Events(entityName = Entity.CORE_META, order = -100)
public class AuthMetaEvents implements SchemaManagerInitListener, RestAPIControllerListener {

    private final AuthModule authModule;
    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;
    private final ApiListenerManagerInit apiListenersManager;

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
                return aop.getUsername();
            }
        }
        return null;
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
    public void onEntityOperationContextCreate(String entity, Object body, HttpServletRequest request, EntityOperationContext entityOperationContext) {
        String remoteUser = request.getRemoteUser();
        AuthEntityOperationContext opContext = new AuthEntityOperationContext(remoteUser);
        entityOperationContext.putModuleEntityOpContext(authModule, opContext);
    }
}

