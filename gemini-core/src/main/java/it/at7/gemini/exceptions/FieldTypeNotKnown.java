package it.at7.gemini.exceptions;

import it.at7.gemini.dsl.entities.RawEntity;

public class FieldTypeNotKnown extends RuntimeException{
    public FieldTypeNotKnown(String model, String type, RawEntity.Entry entry) {
        super(String.format("Field SearchType %s not Know for %s : %s", type, model, entry));
    }
}
