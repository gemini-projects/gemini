package it.at7.gemini.exceptions;

public class InvalidLogicalKeyValue extends RuntimeException {

    public InvalidLogicalKeyValue() {
    }

    public InvalidLogicalKeyValue(String message) {
        super(message);
    }

    public static InvalidLogicalKeyValue KEY_FIELD_NOTEXISTS(String key) {
        return new InvalidLogicalKeyValue(String.format("Logical Key %s not found", key));
    }

    public static final InvalidLogicalKeyValue INVALID_VALUE_TYPE = new InvalidLogicalKeyValue("Invalid value for Logical Key Type");

}
