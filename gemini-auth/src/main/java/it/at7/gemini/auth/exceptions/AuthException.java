package it.at7.gemini.auth.exceptions;

import it.at7.gemini.exceptions.GeminiException;

public class AuthException extends GeminiException {

    public enum Code {
        ADMIN_REQUIRED,
        CANNOT_CHANGE_USERNAME,
        OPEARTION_NOT_PERMITTED_FOR_USER
    }

    public AuthException(AuthException.Code code, String message) {
        super(code.name(), message);
    }

    public static AuthException ADMIN_REQUIRED() {
        return new AuthException(Code.ADMIN_REQUIRED, "The operation can be performed by an Admin");
    }

    public static AuthException CANNOT_CHANGE_USERNAME() {
        return new AuthException(Code.CANNOT_CHANGE_USERNAME, "Username cannot be changed");
    }

    public static AuthException OPEARTION_NOT_PERMITTED_FOR_USER(String username) {
        return new AuthException(Code.OPEARTION_NOT_PERMITTED_FOR_USER, String.format("Operation not permitted for user %s", username));
    }
}
