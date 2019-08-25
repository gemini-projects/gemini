package it.at7.gemini.exceptions;

public class GeminiException extends Exception {
    String errorCodeName;

    public GeminiException(String errorCodeName) {
        super(errorCodeName);
        this.errorCodeName = errorCodeName;
    }

    public GeminiException(String errorCodeName, String message) {
        super(message);
        this.errorCodeName = errorCodeName;
    }

    public GeminiException(String errorCodeName, Exception e) {
        super(e);
        this.errorCodeName = errorCodeName;
    }

    public String getErrorCodeName() {
        return errorCodeName;
    }

    public boolean is(Enum code) {
        return errorCodeName.equalsIgnoreCase(code.name());
    }
}
