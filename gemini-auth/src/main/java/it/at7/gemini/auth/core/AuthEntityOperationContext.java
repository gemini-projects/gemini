package it.at7.gemini.auth.core;

import it.at7.gemini.core.EntityOperationContext;
import it.at7.gemini.core.EntityRecord;
import it.at7.gemini.core.events.EventContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AuthEntityOperationContext {
    private EntityRecord user;
    private String username;

    public AuthEntityOperationContext(EntityRecord user) {
        this.user = user;
        this.username = user.get(UserRef.FIELDS.USERNAME);
    }

    public AuthEntityOperationContext(String username) {
        this.user = null;
        this.username = username;
    }

    @Nullable
    public EntityRecord getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }


    public static Optional<AuthEntityOperationContext> extractAuthOperationContext(EventContext eventContext) {
        Optional<EntityOperationContext> entityOperationContext = eventContext.getEntityOperationContext();
        if (entityOperationContext.isPresent()) {
            EntityOperationContext eop = entityOperationContext.get();
            Optional<AuthEntityOperationContext> authEntityOpContext = eop.getModuleEntityOpContext("AUTH");
            if (authEntityOpContext.isPresent()) {
                return Optional.of(authEntityOpContext.get());
            }
        }
        return Optional.empty();
    }

}
