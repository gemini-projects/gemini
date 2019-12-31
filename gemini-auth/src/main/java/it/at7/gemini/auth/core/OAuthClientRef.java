package it.at7.gemini.auth.core;

public interface OAuthClientRef {
    String NAME = "OAUTHCLIENT";

    interface FIELDS {
        String CLIENT_ID = "clientId";
        String CLIENT_SECRET = "clientSecret";
    }
}
