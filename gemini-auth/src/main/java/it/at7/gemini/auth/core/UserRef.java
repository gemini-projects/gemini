package it.at7.gemini.auth.core;

public interface UserRef {
    String NAME = "USER";

    interface FIELDS {
        String USERNAME = "username";
        String DISPLAY_NAME = "displayName";
        String DESCRIPTION = "description";
        String FRAMEWORK = "framework";
        String PASSWORD = "password";
    }
}
