package it.at7.gemini.core;

import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.EntityField;

import java.util.Collection;
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
     * @param transaction the transaction to use
     * @throws GeminiException
     */
    void initializeSchemaStorage(Transaction transaction) throws GeminiException;

    /**
     * It should be be called after {@link #initializeSchemaStorage(Transaction)} and performs all the actions to create
     * schema Entity Records (Entity, Fields, and so on...)
     *
     * @param transaction the transaction to use
     * @throws GeminiException
     */
    void initializeSchemaEntityRecords(Transaction transaction) throws GeminiException;

    /**
     * Initialization schema actions can be done with a suitable EntityOperationContext
     *
     * @return the context to use in initialization schema resourcess
     */
    EntityOperationContext getOperationContextForInitSchema();


    /**
     * Iterate throw the field list to register its deletion bu using {@link #addEntityFieldsToDelete(EntityField)}
     *
     * @param entityFieldList list ot Fields
     */
    default void addEntityFieldsToDelete(Collection<EntityField> entityFieldList) {
        entityFieldList.forEach(this::addEntityFieldsToDelete);
    }

    /**
     * Add an Entity Field to consider when the SchemaManager performs Framework Entities deletion. This type of delete
     * doesn't follow the EntityManager Hooks, but instead foreign Entities are removed by low level persistence delete
     * EX: When an Entity was removed from the schema, we should also remove linked entities
     *
     * @param entityField the EntityField to consider for record delete
     */
    void addEntityFieldsToDelete(EntityField entityField);


    /**
     * Initialize a Dynamic Schema.
     *
     * @param module
     * @param rawSchema
     * @param operationContext
     * @param transaction
     * @throws GeminiException
     */
    void initDynamicSchema(ModuleBase module, RawSchema rawSchema, EntityOperationContext operationContext, Transaction transaction) throws GeminiException;
}
