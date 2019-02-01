package it.at7.gemini.exceptions;

import static it.at7.gemini.exceptions.InvalidRequesException.Code.*;

public class InvalidRequesException extends GeminiException {
    enum Code {
        INVALID_METHOD_FOR_REQUEST,
        BODY_REQUIRED,
        CANNOT_HANDLE_REQUEST,
        INVALID_BODY
    }

    public InvalidRequesException(Code errorCode, String message) {
        super(errorCode.name(), message);
    }

    public static InvalidRequesException INVALID_METHOD_FOR_REQUEST(String method) {
        return new InvalidRequesException(INVALID_METHOD_FOR_REQUEST, String.format("Invalid method % for request", method));
    }

    public static InvalidRequesException BODY_REQUIRED() {
        return new InvalidRequesException(BODY_REQUIRED, String.format("Body required for request"));
    }

    public static InvalidRequesException INVALID_BODY() {
        return new InvalidRequesException(INVALID_BODY, String.format("Body required for request"));
    }

    public static InvalidRequesException CANNOT_HANDLE_REQUEST() {
        return new InvalidRequesException(CANNOT_HANDLE_REQUEST, "Cannot handle Request");
    }
}
