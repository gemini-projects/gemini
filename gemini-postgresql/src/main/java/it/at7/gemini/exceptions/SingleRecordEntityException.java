package it.at7.gemini.exceptions;

import it.at7.gemini.schema.Entity;

public class SingleRecordEntityException extends GeminiException {
    public SingleRecordEntityException(Entity entity) {
        super(String.format("Entity %s has more than one record", entity.getName()));
    }
}
