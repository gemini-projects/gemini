package it.at7.gemini.exceptions;

public class GeminiException extends Exception {
    String errorCodeName;

    protected GeminiException(String errorCodeName) {
        super(errorCodeName);
        this.errorCodeName = errorCodeName;
    }

    protected GeminiException(String errorCodeName, String message) {
        super(message);
        this.errorCodeName = errorCodeName;
    }

    protected GeminiException(String errorCodeName, Exception e) {
        super(e);
        this.errorCodeName = errorCodeName;
    }

    public String getErrorCodeName() {
        return errorCodeName;
    }

}
