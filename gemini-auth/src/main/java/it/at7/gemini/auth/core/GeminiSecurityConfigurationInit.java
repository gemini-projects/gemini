package it.at7.gemini.auth.core;

public interface GeminiSecurityConfigurationInit {

    /**
     * Add a public url to Gemini Authorization chain. This route will not require Auth/Access Tokens
     *
     * @param url target url
     */
    void addPublicUrl(String url);
}
