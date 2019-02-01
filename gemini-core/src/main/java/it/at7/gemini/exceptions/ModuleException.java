package it.at7.gemini.exceptions;

public class ModuleException extends GeminiException {
    ModuleException(String message) {
        super(message);
    }

    public static  ModuleException NOT_FOUND(String modelName){
        return new ModuleException(String.format("Module %s not found", modelName));
    }

    public  static ModuleException NOT_EDITABLE_MODULE(String modelName) {
        return new ModuleException(String.format("Module %s not is not editable", modelName));
    }

    public  static ModuleException NEED_MODULE(String modelName) {
        return new ModuleException(String.format("Module %s not is not editable", modelName));
    }
}

