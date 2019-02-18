package it.at7.gemini.exceptions;

public class GeminiRuntimeException extends RuntimeException {

    public GeminiRuntimeException(String message) {
        super(message);
    }

    public GeminiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
