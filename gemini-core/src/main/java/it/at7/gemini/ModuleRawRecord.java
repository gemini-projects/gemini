package it.at7.gemini;

import it.at7.gemini.dsl.entities.EntityRawRecords;
import it.at7.gemini.dsl.entities.RawEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Store all the record definitions found in a module. So both the MODULE.at and SINGLE_ENTITY.at
 */
public class ModuleRawRecord {
    // TODO convert to builder or immutable

    private Map<String, EntityRawRecords> moduleRecordsByEntity = new HashMap<>();

    // EntityFile -> ( EntityName , RawRecords )
    private Map<String, Map<String, EntityRawRecords>> singleEntityRecordsDefinition = new HashMap<>();

    public ModuleRawRecord() {
    }


    public void addModuleRecords(Map<String, EntityRawRecords> moduleRecordsByEntity) {
        this.moduleRecordsByEntity = moduleRecordsByEntity;
    }


    public void addEntityRecords(RawEntity rawEntity, Map<String, EntityRawRecords> specificResourceRecords) {
        String entityName = rawEntity.getName().toUpperCase();
        singleEntityRecordsDefinition.put(entityName, specificResourceRecords);
    }

    public Map<String, EntityRawRecords> getModuleRecordsByEntity() {
        return moduleRecordsByEntity;
    }

    public Map<String, Map<String, EntityRawRecords>> getSingleEntityRecordsDefinition() {
        return singleEntityRecordsDefinition;
    }
}
