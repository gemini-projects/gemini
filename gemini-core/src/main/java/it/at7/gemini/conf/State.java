package it.at7.gemini.conf;

public enum State implements Comparable<State> {
    STARTING,
    STARTED,
    SCHEMA_CHEKING,
    SCHEMA_INITIALIZED,
    EVENTS_LOADED,
    INITIALIZED,
    API_INITIALIZATION,
    API_INITIALIZED,
    WEB_APP_INITIALIZATION,
    WEB_APP_INITIALIZED,
    VALID_SCHEMA,
    READY
}
