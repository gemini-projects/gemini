package it.at7.gemini.core;

import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;

import java.util.List;
import java.util.Map;

public interface SchemaManagerInit {

    /**
     * Load Gemini Modules Schemas
     *
     * @param modules list of GeminiModule
     * @throws GeminiException
     */
    void loadGeminiModulesSchemas(List<GeminiModule> modules) throws GeminiException;

    /**
     * Before initialize storage we could add some external schemas, for example coming from custom Modules
     *
     * @param rawSchemas a map for the base module to RawSchema
     */
    void addExternalSchemas(Map<ModuleBase, RawSchema> rawSchemas);

    /**
     * It must be called after {@link #loadGeminiModulesSchemas(List)} and {@link #addExternalSchemas(Map)} in order
     * to initialize schema storage loaded by the Schema Init service
     *
     * @param transaction
     * @throws GeminiException
     */
    void initializeSchemaStorage(Transaction transaction) throws GeminiException;

    void initializeSchemaEntityRecords(Transaction transaction) throws GeminiException;

    EntityOperationContext getOperationContextForInitSchema();
}
