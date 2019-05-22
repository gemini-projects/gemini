package it.at7.gemini.exceptions;

public class GeminiRuntimeException extends RuntimeException {

    public GeminiRuntimeException(String message) {
        super(message);
    }

    public GeminiRuntimeException(Throwable cause) {
        super(cause);
    }

    public GeminiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
