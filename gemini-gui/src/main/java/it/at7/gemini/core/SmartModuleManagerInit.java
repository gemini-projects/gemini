package it.at7.gemini.core;

import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.schema.SmartSchemaWrapper;
import it.at7.gemini.schema.smart.SmartSchema;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public interface SmartModuleManagerInit {

    /**
     * load smart resources schemas
     *
     * @param modulesInOrder smart schemas in order
     * @param transaction
     * @return
     * @throws GeminiException exception
     */
    LinkedHashMap<SmartModule, SmartSchemaWrapper> loadSmartResources(List<SmartModule> modulesInOrder, Transaction transaction) throws GeminiException;

    /**
     * Initialize Smart Modules Framework Entity Records
     *
     * @param smartSchemaByModule module - schema
     * @param transaction
     */
    void initializeSmartModulesRecords(LinkedHashMap<SmartModule, SmartSchemaWrapper> smartSchemaByModule, Transaction transaction) throws GeminiException;

    /**
     * Return the mapper to handle Smart Module input
     *
     * @param text
     * @return YAML object mapper
     */
    SmartSchema parseSmartModule(String text) throws IOException;

}
