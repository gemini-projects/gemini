package it.at7.gemini.exceptions;

public class SmartModuleException extends GeminiException {
    public enum Code {
        SMART_MODULE_NOT_FOUND,
    }

    public SmartModuleException(Code errorCode, String message) {
        super(errorCode.name(), message);
    }

    public SmartModuleException SMART_MODULE_NOT_FOUND(String moduleName) {
        return new SmartModuleNotFound(Code.SMART_MODULE_NOT_FOUND, moduleName);
    }

    public static class SmartModuleNotFound extends SmartModuleException {
        public SmartModuleNotFound(Code errorCode, String message) {
            super(errorCode, message);
        }
    }
}
