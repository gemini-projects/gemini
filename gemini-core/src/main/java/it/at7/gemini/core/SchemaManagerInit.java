package it.at7.gemini.core;

import java.util.List;

public interface SchemaManagerInit {

    void initializeSchemas(List<Module> modulesInOrder) throws Exception;

}
