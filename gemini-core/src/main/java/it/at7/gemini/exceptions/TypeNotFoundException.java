package it.at7.gemini.exceptions;

public class TypeNotFoundException extends RuntimeException {
    public TypeNotFoundException(String model, String type) {
        super(String.format("SearchType %s not found in model %s", type, model));
    }
}
