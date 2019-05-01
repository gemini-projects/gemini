package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;

import java.util.List;

public interface SchemaManagerInit {

    void initializeSchemasStorage(List<Module> modulesInOrder, Transaction transaction) throws GeminiException;

    void initializeSchemaEntityRecords(List<Module> modulesInOrder, Transaction transaction) throws GeminiException;

}
