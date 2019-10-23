package it.at7.gemini.exceptions;

public class ModuleException extends GeminiException {

    public enum Code {
        MODULE_NOT_FOUND,
    }

    public ModuleException(Code erroCodeName, String message) {
        super(erroCodeName.name(), message);
    }

    public ModuleNotFound MODULE_NOT_FOUND(String moduleName) {
        return new ModuleNotFound(Code.MODULE_NOT_FOUND, moduleName);
    }

    public static class ModuleNotFound extends ModuleException {
        public ModuleNotFound(Code errorCode, String message) {
            super(errorCode, message);
        }
    }

}

