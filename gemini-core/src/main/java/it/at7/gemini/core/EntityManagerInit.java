package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.Entity;

public interface EntityManagerInit {

    EntityRecord createOneRecordEntityRecord(Entity entity, EntityOperationContext entityOperationContext, Transaction transaction) throws GeminiException;

}
