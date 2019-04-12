package it.at7.gemini.exceptions;

public class SchemaException extends GeminiException {
    public enum Code {
        DYNAMIC_SCHEMA_NOT_ENABLED
    }

    public SchemaException(SchemaException.Code code, String message) {
        super(code.name(), message);
    }

    public static SchemaException DYNAMIC_SCHEMA_NOT_ENABLED(String entity) {
        return new SchemaException(Code.DYNAMIC_SCHEMA_NOT_ENABLED, String.format("Dynamic schema not allowed on entity %s", entity));
    }
}
