package it.at7.gemini.core;

import it.at7.gemini.ModuleRawRecord;
import it.at7.gemini.conf.State;
import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.core.persistence.PersistenceSchemaManager;
import it.at7.gemini.dsl.RecordParser;
import it.at7.gemini.dsl.SchemaParser;
import it.at7.gemini.dsl.entities.EntityRawRecords;
import it.at7.gemini.dsl.entities.RawEntity;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.dsl.entities.RawSchemaBuilder;
import it.at7.gemini.exceptions.*;
import it.at7.gemini.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static it.at7.gemini.core.EntityManagerImpl.CORE_ENTITIES;
import static it.at7.gemini.core.FilterContext.ALL;
import static it.at7.gemini.schema.FieldType.*;
import static java.util.stream.Collectors.toList;

@Service
public class SchemaManagerImpl implements SchemaManager, SchemaManagerInit {
    private static final Logger logger = LoggerFactory.getLogger(SchemaManagerImpl.class);
    private static final String INIT_SCHEMA_MESSAGE_RESOURCE = "classpath:/messages/InitSchema.txt";

    private final ApplicationContext applicationContext;
    private final StateManager stateManager;
    private final PersistenceSchemaManager persistenceSchemaManager;
    private final PersistenceEntityManager persistenceEntityManager;
    private final GeminiConfigurationService geminiConfigurationService;
    private final EntityManagerImpl entityManager;

    private Map<String, GeminiModule> geminiModules;
    private List<GeminiModule> geminiOrderedModules; // TODO check if it can be refactored or removed
    private Map<GeminiModule, RawSchema> geminiSchemas = new HashMap<>();
    private Map<ModuleBase, RawSchema> externalSchema = new HashMap<>();


    // entities are stored UPPERCASE
    private Map<String, Entity> entities = new LinkedHashMap<>();
    private Map<GeminiModule, ModuleRawRecord> schemaRawRecordsByGeminiModule;

    @Autowired
    public SchemaManagerImpl(ApplicationContext applicationContext,
                             StateManager stateManager,
                             PersistenceSchemaManager persistenceSchemaManager,
                             PersistenceEntityManager persistenceEntityManager,
                             GeminiConfigurationService geminiConfigurationService,
                             @Lazy EntityManagerImpl entityManager) {
        this.applicationContext = applicationContext;
        this.stateManager = stateManager;
        this.persistenceSchemaManager = persistenceSchemaManager;
        this.persistenceEntityManager = persistenceEntityManager;
        this.geminiConfigurationService = geminiConfigurationService;
        this.entityManager = entityManager;
    }

    @Override
    public void loadGeminiModulesSchemas(List<GeminiModule> geminiModules) throws GeminiException {
        this.geminiModules = geminiModules.stream().collect(Collectors.toMap(m -> m.getName().toUpperCase(), m -> m));
        this.geminiOrderedModules = this.geminiModules.values().stream().sorted(Comparator.comparingInt(GeminiModule::order)).collect(toList());
        this.geminiSchemas.putAll(loadGeminiModuleSchemas(geminiModules));
        this.schemaRawRecordsByGeminiModule = loadGeminiModuleRecords(geminiModules);
    }

    @Override
    public void addExternalSchemas(Map<ModuleBase, RawSchema> rawSchemas) {
        this.externalSchema.putAll(rawSchemas);
    }

    @Override
    public void initializeSchemaStorage(Transaction transaction) throws GeminiException {
        persistenceSchemaManager.beforeLoadSchema(transaction);

        Map<ModuleBase, RawSchema> allSchemasByBaseModule = new LinkedHashMap<>();
        allSchemasByBaseModule.putAll(this.geminiSchemas);
        allSchemasByBaseModule.putAll(this.externalSchema);

        checkSchemaAndCreateEntities(allSchemasByBaseModule);
        persistenceSchemaManager.handleSchemaStorage(transaction, entities.values()); // create storage for entities
    }

    @Override
    public void initializeSchemaEntityRecords(Transaction transaction) throws GeminiException {

        /* NB: dediced to use basic data types (no references) for common Entity/Field column types... so no need to initializeSmartModules anything
        //
        // lets first of all create records (domains) required by Entity/Fields... (special entities)
        // entityRecordForHardCodedEntity(transaction, recordsByEntity, EntityRef.NAME);
        // entityRecordForHardCodedEntity(transaction, recordsByEntity, FieldRef.NAME);
        */
        handleSchemasEntityRecords(transaction); // add core entityRecord i.e. ENTITY and FIELD
        stateManager.changeState(State.FRAMEWORK_SCHEMA_RECORDS_INITIALIZED, Optional.of(transaction));
        createProvidedEntityRecords(transaction); // add entity record provided as resources
        loadEntityRecordsForFrameworkEntities(transaction); // reload entity record and assign them to framework objects
        stateManager.changeState(State.PROVIDED_CLASSPATH_RECORDS_HANDLED, Optional.of(transaction));
    }

    @Override
    @Nullable
    public Entity getEntity(String entity) {
        return entities.get(entity.toUpperCase());
    }

    @Override
    public Collection<ModuleBase> getAllModules() {
        List<ModuleBase> res = new ArrayList<>();
        res.addAll(geminiOrderedModules);
        res.addAll(externalSchema.keySet());
        return Collections.unmodifiableList(res);
    }

    @Override
    public List<EntityField> getEntityReferenceFields(Entity targetEntity) {
        return this.entities.values().stream()
                .flatMap(e -> e.getDataEntityFields().stream())
                .filter(f -> f.getType().equals(FieldType.ENTITY_REF))
                .filter(f -> Objects.nonNull(f.getEntityRef()) && f.getEntityRef().equals(targetEntity))
                .collect(Collectors.toList());
    }


    private void handleSchemasEntityRecords(Transaction transaction) throws GeminiException {
        EntityOperationContext entityOperationContext = getOperationContextForInitSchema();
        for (Entity entity : entities.values()) {
            updateENTITYRecord(transaction, entityOperationContext, entity);
        }
        persistenceSchemaManager.deleteUnnecessaryEntites(entities.values(), transaction);
        for (Entity entity : entities.values()) {
            List<EntityRecord> erFields = updateEntityFieldsRecords(transaction, entityOperationContext, entity, null);

            persistenceSchemaManager.deleteUnnecessaryFields(entity, erFields, transaction);

            // singleton entities
            handleOneRecordEntity(transaction, entityOperationContext, entity);
        }
    }

    private void handleOneRecordEntity(Transaction transaction, EntityOperationContext entityOperationContext, Entity entity) throws GeminiException {
        if (entity.isOneRecord()) {
            try {
                entityManager.getOneRecordEntity(entity, entityOperationContext, transaction);
            } catch (EntityRecordException e) {
                if (e.getErrorCodeName().equals(EntityRecordException.Code.ONERECORD_ENTITY_MUSTEXIST.name())) {
                    entityManager.createOneRecordEntityRecord(entity, entityOperationContext, transaction);
                } else {
                    throw e;
                }
            }
        }
    }

    private void loadEntityRecordsForFrameworkEntities(Transaction transaction) throws GeminiException {
        List<EntityRecord> allEntities = entityManager.getRecordsMatching(getEntity(EntityRef.NAME), ALL, getOperationContextForInitSchema(), transaction);
        allEntities.forEach(e -> {
            Entity entity = entityManager.getEntity(e.get(EntityRef.FIELDS.NAME));
            assert entity != null;
            entity.actualEntityRecord(e);
        });
    }

    @Override
    public EntityOperationContext getOperationContextForInitSchema() {
        EntityOperationContext entityOperationContext = new EntityOperationContext();

        Map<String, SchemaManagerInitListener> listenerMap = applicationContext.getBeansOfType(SchemaManagerInitListener.class);

        Collection<SchemaManagerInitListener> listeners = listenerMap.values();
        // TODO add sort ? by gemini module ?

        for (SchemaManagerInitListener l : listeners) {
            l.onSchemasEntityRecords(entityOperationContext);
        }
        return entityOperationContext;
    }

    private List<EntityRecord> updateEntityFieldsRecords(Transaction transaction, EntityOperationContext entityOperationContext, Entity entity, @Nullable RootEntityField parentFieldWrapper) throws GeminiException {
        List<EntityRecord> fieldRecords = new ArrayList<>();
        Set<EntityField> fields = entity.getALLEntityFields();
        for (EntityField field : fields) {
            long fieldID = 0;
            EntityRecord fieldEntityRecord;
            if (parentFieldWrapper == null) {
                logger.info("{}: creating/updating EntityRecord Fields for {} : {}", entity.getModule().getName(), entity.getName(), field.getName());
                fieldEntityRecord = field.toInitializationEntityRecord();
            } else {
                String fieldName = parentFieldWrapper.getFieldName(field);
                logger.info("{}: creating/updating EntityRecord Fields for {} : {}", entity.getModule().getName(), parentFieldWrapper.parentEntity.getName(), fieldName);
                Entity fieldRef = entities.get(FieldRef.NAME);
                fieldEntityRecord = new EntityRecord(fieldRef);
                fieldEntityRecord.put(FieldRef.FIELDS.NAME, fieldName);
                fieldEntityRecord.put(FieldRef.FIELDS.ENTITY, parentFieldWrapper.parentEntity.getName());
                fieldEntityRecord.put(FieldRef.FIELDS.TYPE, field.getType().name());
                fieldEntityRecord.put(FieldRef.FIELDS.SCOPE, field.getScope());

                fieldEntityRecord.put(FieldRef.FIELDS.PARENT_FIELD, EntityReferenceRecord.fromPKValue(fieldRef, parentFieldWrapper.getParentFieldID()));
                Entity entityRef = field.getEntityRef();
                if (entityRef != null) {
                    fieldEntityRecord.put("refentity", EntityReferenceRecord.fromPKValue(entityRef, entityRef.getIDValue()));
                }
            }

            fieldEntityRecord = this.entityManager.putOrUpdate(fieldEntityRecord, entityOperationContext, transaction);
            fieldID = fieldEntityRecord.get(fieldEntityRecord.getEntity().getIdEntityField());
            field.setFieldIDValue(fieldID);
            fieldRecords.add(fieldEntityRecord);

            // add also inner fields if it is an embedable field
            if (field.getType().equals(ENTITY_EMBEDED)) {
                Entity entityRef = field.getEntityRef();
                assert entityRef != null;
                RootEntityField newParentField = RootEntityField.fromParent(parentFieldWrapper, entity, field, fieldID);
                fieldRecords.addAll(updateEntityFieldsRecords(transaction, entityOperationContext, entityRef, newParentField));
            }
        }
        return fieldRecords;
    }

    private void updateENTITYRecord(Transaction transaction, EntityOperationContext entityOperationContext, Entity entity) throws GeminiException {
        logger.info("{}: creating/updating EntityRecord for {}", entity.getModule().getName(), entity.getName());
        EntityRecord entityRecord = entity.toInitializationEntityRecord();
        entityRecord = entityManager.putOrUpdate(entityRecord, entityOperationContext, transaction);
        entity.setFieldIDValue(entityRecord.get(entity.getIdEntityField()));
    }


    private void iterateThroughtRawEntitySchemas(Map<ModuleBase, RawSchema> schemas, BiConsumer<ModuleBase, RawEntity> action) {
        schemas.entrySet().forEach(entry -> {
            ModuleBase module = entry.getKey();
            RawSchema rawSchema = entry.getValue();
            rawSchema.getRawEntities().forEach(model -> action.accept(module, model));
        });
    }

    private void iterateThroughtRawInterfaces(Map<ModuleBase, RawSchema> schemas, BiConsumer<ModuleBase, RawEntity> action) {
        schemas.entrySet().forEach(entry -> {
            ModuleBase module = entry.getKey();
            RawSchema rawSchema = entry.getValue();
            rawSchema.getRawEntityInterfaces().forEach(model -> action.accept(module, model));
        });
    }

    // TODO capire come gestire i default per i valueStragy
    /* private void setDefaultsForFields(Map<String, List<EntityRecord>> fieldRecordsByEntityName, Transaction transaction) throws GeminiException {
        Entity fieldevents = this.entities.get("FIELDEVENTS");
        assert fieldevents != null;
        EntityRecord fieldEntityRecord = fieldevents.getDefaultEntityRecord();
        for (Map.Entry<String, List<EntityRecord>> entrySet : fieldRecordsByEntityName.entrySet()) {
            String entityName = entrySet.getKey();
            for (EntityRecord fieldRecord : entrySet.getValue()) {
                boolean needDefault = fieldRecord.get("events") == null;
                logger.info(String.format("Handling default fields for entity %s and field %s - default %b", entityName, fieldRecord.get("name"), needDefault));
                if (needDefault) {
                    fieldRecord.put("events", fieldEntityRecord);
                    persistenceEntityManager.updateEntityRecordByID(fieldRecord, transaction);
                }
            }
        }

    } */

    @Override
    public Collection<Entity> getAllEntities() {
        return Collections.unmodifiableCollection(entities.values());
    }

    private void createProvidedEntityRecords(Transaction transaction) throws GeminiException {
        for (GeminiModule m : this.geminiOrderedModules) {
            ModuleRawRecord moduleRawRecord = this.schemaRawRecordsByGeminiModule.get(m);

            Map<String, EntityRawRecords> moduleRecordsByEntity = moduleRawRecord.getModuleRecordsByEntity();
            List<EntityRawRecords.VersionedRecords> versionsRecords = moduleRecordsByEntity.values().stream().
                    map(EntityRawRecords::getVersionedRecords).flatMap(v -> v.values().stream()).
                    sorted(Comparator.comparingLong(EntityRawRecords.VersionedRecords::getDefinitionOrder)).collect(toList());

            EntityOperationContext operationContextForInitSchema = getOperationContextForInitSchema();
            for (EntityRawRecords.VersionedRecords version : versionsRecords) {
                String entityName = version.getEntity().toUpperCase();

                if (this.geminiConfigurationService.isDevMode()) {
                    createOrUpdateRecords(transaction, operationContextForInitSchema, version, entityName);
                } else {
                    EntityRecord initVersionRec = new EntityRecord(entities.get(InitRecordDef.NAME));
                    initVersionRec.set(InitRecordDef.FIELDS.ENTITY, entityName);
                    initVersionRec.set(InitRecordDef.FIELDS.VERSION_NAME, version.getVersionName());
                    initVersionRec.set(InitRecordDef.FIELDS.VERSION_NUMBER, version.getVersionProgressive());
                    Optional<EntityRecord> optRecord = entityManager.getOptional(initVersionRec, transaction);

                    if (!optRecord.isPresent()) {
                        createOrUpdateRecords(transaction, operationContextForInitSchema, version, entityName);
                        entityManager.putIfAbsent(initVersionRec, operationContextForInitSchema, transaction);
                    }
                }
            }

        }
    }

    private void createOrUpdateRecords(Transaction transaction, EntityOperationContext operationContextForInitSchema, EntityRawRecords.VersionedRecords version, String entityName) throws GeminiException {
        logger.info(String.format("Handling records for entity %s and version %s - %d", entityName, version.getVersionName(), version.getVersionProgressive()));
        for (Object record : version.getRecords()) {
            Entity entity = entities.get(entityName);
            if (entity == null) {
                throw new GeminiRuntimeException(String.format("Handling records for entity %s and version %s - %d -- Entity %s not found", entityName, version.getVersionName(), version.getVersionProgressive(), entityName));
            }
            EntityRecord entityRecord = RecordConverters.entityRecordFromMap(entity, (Map<String, Object>) record);
            if (CORE_ENTITIES.contains(entityName.toUpperCase())) {
                entityManager.update(entityRecord, operationContextForInitSchema, transaction);
            } else {
                entityManager.putOrUpdate(entityRecord, operationContextForInitSchema, transaction);
            }
        }
    }

    private Map<GeminiModule, RawSchema> loadGeminiModuleSchemas(Collection<GeminiModule> modules) throws GeminiGenericException {
        try {
            Map<GeminiModule, RawSchema> schemas = new LinkedHashMap<>();
            for (GeminiModule module : modules) {
                String location = module.getSchemaResourceLocation();
                Resource resource = applicationContext.getResource(location);
                RawSchema rawSchema;
                if (resource.exists()) {
                    InputStream schemaStream = resource.getInputStream();
                    logger.info("Schema definition found for module {}: location {}", module.getName(), location);
                    rawSchema = SchemaParser.parse(new InputStreamReader(schemaStream));
                } else {
                    logger.info("No schema definition found for module {}: location {}", module.getName(), location);
                    if (module.createSchemaIfNotFound()) {
                        Resource autogeneratedTextResource = applicationContext.getResource(INIT_SCHEMA_MESSAGE_RESOURCE);
                        File targetSchemaFile = new File(module.getSchemaLocation());
                        targetSchemaFile.getParentFile().mkdirs();
                        Files.copy(autogeneratedTextResource.getInputStream(), targetSchemaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    rawSchema = RawSchemaBuilder.EMPTY_SCHEMA;
                }
                schemas.put(module, rawSchema);
            }
            return schemas;
        } catch (Exception e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    private Map<GeminiModule, ModuleRawRecord> loadGeminiModuleRecords(Collection<GeminiModule> modules) throws GeminiGenericException {
        try {
            Map<GeminiModule, ModuleRawRecord> recordsByModule = new HashMap<>();
            for (GeminiModule module : modules) {
                ModuleRawRecord moduleRawRecord = new ModuleRawRecord();
                String location = module.getSchemaRecordResourceLocation();
                Resource resource = applicationContext.getResource(location);
                if (resource.exists()) {
                    logger.info("Records definition found for module {}: location {}", module.getName(), location);
                    InputStream schemaRecordStream = resource.getInputStream();
                    moduleRawRecord.addModuleRecords(RecordParser.parse(new InputStreamReader(schemaRecordStream)));
                } else {
                    logger.info("No module records definition found for module {}: location {}", module.getName(), location);
                }
                RawSchema rawSchema = geminiSchemas.get(module);
                if (rawSchema != null) {
                    for (RawEntity rawEntity : rawSchema.getRawEntities()) {
                        String entityName = rawEntity.getName().toLowerCase();
                        String capitalizedEntityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
                        String entityLocation = module.getEntityRecordResourceLocation(capitalizedEntityName);
                        Resource entityResource = applicationContext.getResource(entityLocation);
                        if (entityResource.exists()) {
                            logger.info("Found entity records definition for module/entity {}/{}: location {}", module.getName(), capitalizedEntityName, entityLocation);
                            InputStream entityRecordStream = entityResource.getInputStream();
                            Map<String, EntityRawRecords> specificResourceRecords = RecordParser.parse(new InputStreamReader(entityRecordStream));
                            moduleRawRecord.addEntityRecords(rawEntity, specificResourceRecords);
                            // mergeModuleRecordsWithSpecificEntityRecords(moduleRecords, specificResourceRecords);
                        } else {
                            logger.info("No entity records definition found for module/entity {}/{}: location {}", module.getName(), capitalizedEntityName, entityLocation);
                        }
                    }
                }
                recordsByModule.put(module, moduleRawRecord);
            }
            return recordsByModule;
        } catch (Exception e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    private void checkSchemaAndCreateEntities(Map<ModuleBase, RawSchema> geminiSchemas) throws FieldException {
        /* TODO entità estendibii per modulo
            caricare ogni modulo a se stante.. con le entità.. poi fare il merge delle
            entries (ognuna contenente il modulo da dove viene)
            ... vale anche per i record
        */

        // first of all get all the interfaces and entities (to resolve dependencies without ordering)
        Map<String, EntityBuilder> interfaceBuilders = new HashMap<>();
        Map<String, EntityBuilder> entityBuilders = new HashMap<>();
        iterateThroughtRawInterfaces(geminiSchemas,
                (ModuleBase module, RawEntity rawEntityInterface) -> {
                    String interfaceName = rawEntityInterface.getName().toUpperCase();
                    if (interfaceBuilders.keySet().contains(interfaceName)) {
                        EntityBuilder alreadyExistentEB = interfaceBuilders.get(interfaceName);
                        alreadyExistentEB.addExtraEntity(rawEntityInterface, module);
                    } else {
                        interfaceBuilders.put(interfaceName, new EntityBuilder(rawEntityInterface, module));
                    }
                });
        iterateThroughtRawEntitySchemas(geminiSchemas, (ModuleBase module, RawEntity rawEntity) -> {
            String entityName = rawEntity.getName().toUpperCase();
            EntityBuilder entityB;
            if (entityBuilders.keySet().contains(entityName)) {
                entityB = entityBuilders.get(entityName);
                entityB.addExtraEntity(rawEntity, module);
            } else {
                entityB = new EntityBuilder(rawEntity, module);
                entityBuilders.put(entityName, entityB);
            }


            for (GeminiModule m : geminiOrderedModules) {
                // calculate the default entity record
                String entityUpper = entityName.toUpperCase();
                ModuleRawRecord moduleRawRecord = schemaRawRecordsByGeminiModule.get(m);
                if (moduleRawRecord != null) {
                    Map<String, EntityRawRecords> moduleRecordsByEntity = moduleRawRecord.getModuleRecordsByEntity();
                    EntityRawRecords entityRawRecords = moduleRecordsByEntity.get(entityUpper);
                    if (entityRawRecords != null) {
                        Object defaultRecord = entityRawRecords.getDefaultRecord();
                        if (defaultRecord != null)
                            entityB.setDefaultRecord(defaultRecord);
                    }
                    Map<String, Map<String, EntityRawRecords>> singleEntityRecordsDefinition = moduleRawRecord.getSingleEntityRecordsDefinition();
                    // we get the default only from the specific entity file
                    entityRawRecords = singleEntityRecordsDefinition.getOrDefault(entityUpper, Map.of()).get(entityUpper);
                    if (entityRawRecords != null) {
                        Object defaultRecord = entityRawRecords.getDefaultRecord();
                        if (defaultRecord != null)
                            entityB.setDefaultRecord(defaultRecord);
                    }
                }
            }
        });

        // now we can resolve entity fields
        for (Map.Entry<String, EntityBuilder> entityEntry : entityBuilders.entrySet()) {
            EntityBuilder currentEntityBuilder = entityEntry.getValue();

            // add the meta information to the current entity
            // NB: (CORE_META must be found... otherwise its ok to have a null runtime excp)
            EntityBuilder metaIntBuilder = interfaceBuilders.get(Entity.CORE_META);
            addAllEntriesToEntityBuilder(entityBuilders, metaIntBuilder.getRawEntity(), currentEntityBuilder, Entity.CORE_META, EntityField.Scope.META);
            for (EntityBuilder.ExtraEntity externalEntity : metaIntBuilder.getExternalEntities()) {
                RawEntity extRawEntity = externalEntity.getRawEntity();
                ModuleBase extModule = externalEntity.getModule(); // TODO add MODULE to FIELD
                addAllEntriesToEntityBuilder(entityBuilders, extRawEntity, currentEntityBuilder, Entity.CORE_META, EntityField.Scope.META);
            }

            // merging Gemini interface if found
            addALLImplementingInterfaceToEntityBuilder(entityBuilders, currentEntityBuilder, currentEntityBuilder.getRawEntity(), interfaceBuilders);

            // root module fields
            addAllEntriesToEntityBuilder(entityBuilders, currentEntityBuilder.getRawEntity(), currentEntityBuilder, null, EntityField.Scope.DATA);
            for (EntityBuilder.ExtraEntity externalEntity : currentEntityBuilder.getExternalEntities()) {
                addALLImplementingInterfaceToEntityBuilder(entityBuilders, currentEntityBuilder, externalEntity.getRawEntity(), interfaceBuilders);
                addAllEntriesToEntityBuilder(entityBuilders, externalEntity.getRawEntity(), currentEntityBuilder, null, EntityField.Scope.DATA);
            }
        }

        // now we can build the final entity
        Map<String, Entity> entities = new LinkedHashMap<>();
        for (EntityBuilder entityBuilder : entityBuilders.values()) {
            Entity entity = entityBuilder.build();
            entities.put(entityBuilder.getName(), entity);
        }

        this.entities = entities;
    }

    private void addALLImplementingInterfaceToEntityBuilder(Map<String, EntityBuilder> allEntityBuilders, EntityBuilder currentEntityBuilder, RawEntity rawEntityWIthInterfaces, Map<String, EntityBuilder> interfaceBuilders) throws FieldException {
        // merging Gemini interface if found
        for (String implementsInteface : rawEntityWIthInterfaces.getImplementsIntefaces()) {
            // entity implements a common specification
            String interfaceName = implementsInteface.toUpperCase();
            EntityBuilder enitityImplementsInterface = interfaceBuilders.get(interfaceName);
            RawEntity rawEntityInterface = enitityImplementsInterface.getRawEntity();
            addAllEntriesToEntityBuilder(allEntityBuilders, rawEntityInterface, currentEntityBuilder, interfaceName, EntityField.Scope.DATA);
            for (EntityBuilder.ExtraEntity externalInterfaceEntity : enitityImplementsInterface.getExternalEntities()) {
                addAllEntriesToEntityBuilder(allEntityBuilders, externalInterfaceEntity.getRawEntity(), currentEntityBuilder, interfaceName, EntityField.Scope.DATA);
            }
        }
    }

    private void addAllEntriesToEntityBuilder(Map<String, EntityBuilder> allEntityBuilders, RawEntity entity, EntityBuilder currentEntityBuilder, String interfaceName, EntityField.Scope entityFieldScopes) throws FieldException {
        for (RawEntity.Entry currentEntry : entity.getEntries()) {
            checkAndCreateField(allEntityBuilders, currentEntityBuilder, currentEntry, interfaceName, entityFieldScopes);
        }
    }

    private void checkAndCreateField(Map<String, EntityBuilder> entityBuilders, EntityBuilder entityBuilder, RawEntity.Entry entry, String interfaceName, EntityField.Scope scope) throws TypeNotFoundException, FieldException {
        String type = entry.getType().toUpperCase();

        Optional<FieldType> fieldType = FieldType.of(type);
        if (!fieldType.isPresent()) {
            // it is not a reconducible 1 to 1 type

            // try to get an alias
            Optional<FieldType> aliasOfType = FieldType.getAliasOfType(type);
            if (aliasOfType.isPresent()) {
                entityBuilder.addField(aliasOfType.get(), entry, interfaceName, scope);
                return;
            }

            // try to get a static reference for entity
            EntityBuilder entityForType = entityBuilders.get(type);
            if (entityForType != null) {
                boolean embedable = entityForType.getRawEntity().isEmbedable();
                entityBuilder.addField(embedable ? ENTITY_EMBEDED : ENTITY_REF, entry, entityForType.getName(), interfaceName, scope);
                return;
            }

            // try to get an array of entity ref (NB arrays of basic types are handled withRecord aliases)
            if (type.charAt(0) == '[' && type.charAt(type.length() - 1) == ']') {
                String entityRef = type.substring(1, type.length() - 1);
                EntityBuilder entityForRefType = entityBuilders.get(entityRef);
                if (entityForRefType != null) {
                    // TODO handle embedable entity ref
                    boolean embedable = entityForRefType.getRawEntity().isEmbedable();
                    if (!embedable) {
                        entityBuilder.addField(ENTITY_REF_ARRAY, entry, entityForRefType.getName(), interfaceName, scope);
                        return;
                    }
                }
            }

            throw new FieldTypeNotKnown(entityBuilder.getName(), type, entry);
        } else {
            // check field type before entering in the entiybuilder
            FieldType ft = fieldType.get();
            if (ft.equals(GENERIC_ENTITY_REF) || ft.equals(ENTITY_EMBEDED)) {
                if (entry.isLogicalKey())
                    throw FieldException.CANNOT_BE_LOGICAL_KEY(ft);
            }
            entityBuilder.addField(ft, entry, interfaceName, scope);
        }
    }

    static class RootEntityField {
        List<EntityField> fields;
        Entity parentEntity;
        List<Long> parentsId;

        public RootEntityField(Entity parentEntity, EntityField parent, long fieldID) {
            this.parentEntity = parentEntity;
            this.fields = new ArrayList<>();
            this.fields.add(parent);
            this.parentsId = new ArrayList<>();
            this.parentsId.add(fieldID);
        }

        public RootEntityField(RootEntityField parentFieldWrapper, EntityField field, long fieldID) {
            this.parentEntity = parentFieldWrapper.parentEntity;
            this.fields = new ArrayList<>();
            this.fields.addAll(parentFieldWrapper.fields);
            this.fields.add(field);
            this.parentsId = new ArrayList<>();
            this.parentsId.addAll(parentFieldWrapper.parentsId);
            this.parentsId.add(fieldID);
        }

        public static RootEntityField fromParent(@Nullable RootEntityField parentFieldWrapper, Entity entity, EntityField field, long fieldID) {
            if (parentFieldWrapper == null) {
                return new RootEntityField(entity, field, fieldID);
            }
            return new RootEntityField(parentFieldWrapper, field, fieldID);
        }

        String getFieldName(EntityField field) {
            StringBuilder s = new StringBuilder();
            fields.forEach(f -> s.append(f.getName()).append("."));
            s.append(field.getName());
            return s.toString();
        }

        public Long getParentFieldID() {
            return parentsId.get(parentsId.size() - 1);
        }
    }
}
