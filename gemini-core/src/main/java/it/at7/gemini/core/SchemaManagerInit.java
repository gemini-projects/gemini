package it.at7.gemini.core;

import java.util.List;

public interface SchemaManagerInit {

    void initializeSchemasStorage(List<Module> modulesInOrder, Transaction transaction) throws Exception;

    void initializeSchemaEntityRecords(List<Module> modulesInOrder, Transaction transaction) throws Exception;


}
