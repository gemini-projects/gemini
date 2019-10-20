package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.smart.SmartSchema;

import java.util.LinkedHashMap;
import java.util.List;

public interface SmartModuleManagerInit {

    /**
     * load smart resources schemas
     *
     * @param modulesInOrder smart schemas in order
     * @return
     * @throws GeminiException exception
     */
    LinkedHashMap<SmartModule, SmartSchema> loadSmartResources(List<SmartModule> modulesInOrder) throws GeminiException;

    /**
     * Initialize Smart Modules Framework Entity Records
     *
     * @param smartSchemaByModule module - schema
     * @param transaction
     */
    void initializeSmartModulesRecords(LinkedHashMap<SmartModule, SmartSchema> smartSchemaByModule, Transaction transaction) throws GeminiException;

}
