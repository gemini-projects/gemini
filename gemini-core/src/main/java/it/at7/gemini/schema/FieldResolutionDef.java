package it.at7.gemini.schema;

public interface FieldResolutionDef {
    String NAME = "FIELDRESOLUTION";
    class FIELDS {
        public static final String FIELD = "field";
        public static final String CODE = "code";
        public static final String VALUE = "value";
    }
    enum VALUE {
        DELETE,
        EMPTY
    }
}
