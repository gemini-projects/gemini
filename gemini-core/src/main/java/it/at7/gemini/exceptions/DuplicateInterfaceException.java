package it.at7.gemini.exceptions;

public class DuplicateInterfaceException extends RuntimeException {
    public DuplicateInterfaceException(String name) {
        super(String.format("Duplicate interface found: %s", name));
    }
}
