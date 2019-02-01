package it.at7.gemini.exceptions;

public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String name) {
        super(String.format("Duplicate entity found: %s", name));
    }
}
