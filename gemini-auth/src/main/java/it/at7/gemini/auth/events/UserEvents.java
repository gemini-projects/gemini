package it.at7.gemini.auth.events;

import it.at7.gemini.auth.core.AuthEntityOperationContext;
import it.at7.gemini.auth.core.UserRef;
import it.at7.gemini.auth.exceptions.AuthException;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.events.*;
import it.at7.gemini.exceptions.GeminiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Events(entityName = UserRef.NAME, order = -100)
public class UserEvents {

    @Value("${gemini.auth.userchecks:true}")
    private boolean CHECK_USER;

    @BeforeInsertField(field = "displayName")
    public String defaultDisplayName(EventContext context) {
        EntityRecord entityRecord = context.getEntityRecord();
        String displayName = entityRecord.get("displayName");
        if (StringUtils.isEmpty(displayName)) {
            return entityRecord.get("username");
        }
        return displayName;
    }

    @BeforeUpdateRecord
    public void checkUserPermission(EventContext eventContext) throws GeminiException {
        if (CHECK_USER) {
            Optional<AuthEntityOperationContext> authEntityOperationContext = AuthEntityOperationContext.extractAuthOperationContext(eventContext);
            if (authEntityOperationContext.isPresent()) {
                AuthEntityOperationContext opc = authEntityOperationContext.get();
                Optional<EntityRecord> persistedEntityRecord = eventContext.getPersistedEntityRecord();
                assert persistedEntityRecord.isPresent();
                EntityRecord pr = persistedEntityRecord.get();
                String username = opc.getUsername();
                if (!username.equals("Admin")
                        && !username.equals(pr.get(UserRef.FIELDS.USERNAME))) {
                    throw AuthException.OPEARTION_NOT_PERMITTED_FOR_USER(username);
                }
            }
        }
    }

    @BeforeCreateRecord
    @BeforeDeleteRecord
    public void checkAdminPermission(EventContext eventContext) throws GeminiException {
        if (CHECK_USER) {
            Optional<AuthEntityOperationContext> authEntityOperationContext = AuthEntityOperationContext.extractAuthOperationContext(eventContext);
            if (authEntityOperationContext.isPresent()) {
                if (!authEntityOperationContext.get().getUsername().equals("Admin")) {
                    throw AuthException.ADMIN_REQUIRED();
                }
            }
        }
    }

    @OnUpdateField(field = "username")
    public void cannotUpdateUsername(EventContext context) throws AuthException {
        Optional<EntityRecord> persistedEntityRecord = context.getPersistedEntityRecord();
        assert persistedEntityRecord.isPresent();
        EntityRecord entityRecord = context.getEntityRecord();
        EntityRecord persisted = persistedEntityRecord.get();
        Object userName = entityRecord.get(UserRef.FIELDS.USERNAME);
        if (userName != null && !persisted.get(UserRef.FIELDS.USERNAME).equals(userName)) {
            throw AuthException.CANNOT_CHANGE_USERNAME();
        }
    }
}
