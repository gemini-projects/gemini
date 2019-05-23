package it.at7.gemini.auth;

import it.at7.gemini.core.EntityRecord;
import org.jetbrains.annotations.Nullable;

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

}
