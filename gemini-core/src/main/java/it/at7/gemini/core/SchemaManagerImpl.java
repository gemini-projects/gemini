package it.at7.gemini.core;

import it.at7.gemini.core.persistence.PersistenceEntityManager;
import it.at7.gemini.core.persistence.PersistenceSchemaManager;
import it.at7.gemini.dsl.RecordParser;
import it.at7.gemini.dsl.SchemaParser;
import it.at7.gemini.dsl.SyntaxError;
import it.at7.gemini.dsl.entities.EntityRawRecordBuilder;
import it.at7.gemini.dsl.entities.EntityRawRecords;
import it.at7.gemini.dsl.entities.RawEntity;
import it.at7.gemini.dsl.entities.RawSchema;
import it.at7.gemini.exceptions.*;
import it.at7.gemini.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static it.at7.gemini.schema.FieldType.*;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
public class SchemaManagerImpl implements SchemaManager {
    private static final Logger logger = LoggerFactory.getLogger(SchemaManagerImpl.class);

    private final ApplicationContext applicationContext;
    private final TransactionManager transactionManager;
    private final PersistenceSchemaManager persistenceSchemaManager;
    private final PersistenceEntityManager persistenceEntityManager;

    private Map<String, Module> modules;
    private Map<Module, RawSchema> schemas = new LinkedHashMap<>();

    // entities are stored UPPERCASE
    private Map<String, Entity> entities = new LinkedHashMap<>();

    @Autowired
    public SchemaManagerImpl(ApplicationContext applicationContext, TransactionManager transactionManager, PersistenceSchemaManager persistenceSchemaManager, PersistenceEntityManager persistenceEntityManager) {
        this.applicationContext = applicationContext;
        this.transactionManager = transactionManager;
        this.persistenceSchemaManager = persistenceSchemaManager;
        this.persistenceEntityManager = persistenceEntityManager;
    }

    @Override
    public void initializeSchemas(Map<String, Module> modules) throws Exception {
        this.modules = modules;
        try (Transaction transaction = transactionManager.openTransaction()) {
            persistenceSchemaManager.beforeLoadSchema(modules, transaction);
            loadModuleSchemas(modules.values());
            Map<String, Map<String, EntityRawRecords>> schemaRawRecordsMap = loadModuleRecords(modules.values());
            checkSchemaAndCreateEntitiesCoreObj(schemaRawRecordsMap);
            Map<String, List<EntityRawRecords>> recordsByEntity = schemaRawRecordsMap.values().stream()
                    .flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));

            persistenceSchemaManager.handleSchemaStorage(transaction, entities.values()); // create storage for entities

            // entity records for entity, field / core entities
            Map<String, List<EntityRecord>> fieldRecordsByEntityName = handleSchemasEntityRecords(entities.values(), transaction);

            createProvidedEntityRecords(recordsByEntity, transaction); // add entity record provided
            // setDefaultsForFields(fieldRecordsByEntityName, transaction);
            transaction.commit();
        }
    }

    @Override
    @Nullable
    public Entity getEntity(String entity) {
        return entities.get(entity.toUpperCase());
    }

    @Override
    @Nullable
    public Module getModule(String module) {
        return modules.get(module.toUpperCase());
    }


    @Override
    public List<EntityField> getEntityReferenceFields(Entity targetEntity) {
        return this.entities.values().stream()
                .flatMap(e -> e.getDataEntityFields().stream())
                .filter(f -> f.getType().equals(FieldType.ENTITY_REF))
                .filter(f -> Objects.nonNull(f.getEntityRef()) && f.getEntityRef().equals(targetEntity))
                .collect(Collectors.toList());
    }


    private Map<String, List<EntityRecord>> handleSchemasEntityRecords(Collection<Entity> entities, Transaction transaction) throws SQLException, GeminiException {
        Map<String, List<EntityRecord>> fieldsRecordByEntityName = new HashMap<>();
        for (Entity entity : entities) {
            updateENTITYRecord(transaction, entity);
        }
        persistenceSchemaManager.deleteUnnecessaryEntites(entities, transaction);
        for (Entity entity : entities) {
            fieldsRecordByEntityName.put(entity.getName().toUpperCase(), updateEntityFieldsRecords(transaction, entity));
        }
        return fieldsRecordByEntityName;
    }

    private List<EntityRecord> updateEntityFieldsRecords(Transaction transaction, Entity entity) throws GeminiException {
        List<EntityRecord> fieldRecords = new ArrayList<>();
        Set<EntityField> fields = new HashSet<>(entity.getMetaEntityFields());
        fields.addAll(entity.getDataEntityFields());
        for (EntityField field : fields) {
            logger.info("{}: creating/updating EntityRecord Fields for {} : {}", entity.getModule().getName(), entity.getName(), field.getName());
            EntityRecord fieldEntityRecord = field.toInitializationEntityRecord();
            fieldEntityRecord = persistenceEntityManager.createOrUpdateEntityRecord(fieldEntityRecord, transaction);
            field.setFieldIDValue(fieldEntityRecord.get(fieldEntityRecord.getEntity().getIdEntityField()));
            fieldRecords.add(fieldEntityRecord);
        }
        persistenceSchemaManager.deleteUnnecessaryFields(entity, fields, transaction);
        return fieldRecords;
    }

    private void updateENTITYRecord(Transaction transaction, Entity entity) throws GeminiException {
        logger.info("{}: creating/updating EntityRecord for {}", entity.getModule().getName(), entity.getName());
        EntityRecord entityRecord = entity.toInitializationEntityRecord();
        entityRecord = persistenceEntityManager.createOrUpdateEntityRecord(entityRecord, transaction);
        entity.setFieldIDValue(entityRecord.get(entity.getIdEntityField()));
    }


    private void iterateThroughtRawEntitySchemas(BiConsumer<Module, RawEntity> action) {
        schemas.entrySet().forEach(entry -> {
            Module module = entry.getKey();
            RawSchema rawSchema = entry.getValue();
            rawSchema.getRawEntities().forEach(model -> action.accept(module, model));
        });
    }

    private void iterateThroughtRawInterfaces(BiConsumer<Module, RawEntity> action) {
        schemas.entrySet().forEach(entry -> {
            Module module = entry.getKey();
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

    private void createProvidedEntityRecords(Map<String, List<EntityRawRecords>> recordsByEntity, Transaction transaction) throws GeminiException {
        // TODO topological order
        for (Map.Entry<String, List<EntityRawRecords>> e : recordsByEntity.entrySet()) {
            String key = e.getKey();
            Entity entity = entities.get(key);
            List<EntityRawRecords> rawRecords = e.getValue();
            for (EntityRawRecords rr : rawRecords) {
                Map<String, EntityRawRecords.VersionedRecords> versionedRecords = rr.getVersionedRecords();
                for (EntityRawRecords.VersionedRecords version : versionedRecords.values()) {
                    EntityRecord initVersionRec = new EntityRecord(entities.get(InitRecordDef.NAME));
                    initVersionRec.set(InitRecordDef.FIELDS.ENTITY, entity.getName());
                    initVersionRec.set(InitRecordDef.FIELDS.VERSION_NAME, version.getVersionName());
                    initVersionRec.set(InitRecordDef.FIELDS.VERSION_NUMBER, version.getVersionProgressive());
                    Optional<EntityRecord> optRecord = persistenceEntityManager.getEntityRecordByLogicalKey(initVersionRec, transaction);

                    if (!optRecord.isPresent()) {
                        logger.info(String.format("Handling records for entity %s and version %s - %d", entity.getName(), version.getVersionName(), version.getVersionProgressive()));
                        for (Object record : version.getRecords()) {
                            EntityRecord entityRecord = RecordConverters.entityRecordFromMap(entity, (Map<String, Object>) record);
                            persistenceEntityManager.createOrUpdateEntityRecord(entityRecord, transaction);
                        }
                        persistenceEntityManager.createNewEntityRecord(initVersionRec, transaction);
                    }
                }
            }
        }
    }


    private void loadModuleSchemas(Collection<Module> modules) throws IOException, SyntaxError {
        for (Module module : modules) {
            String location = module.getSchemaResourceLocation();
            Resource resource = applicationContext.getResource(location);
            if (resource.exists()) {
                InputStream schemaStream = resource.getInputStream();
                RawSchema rawSchema = SchemaParser.parse(new InputStreamReader(schemaStream));
                schemas.put(module, rawSchema);
            } else {
                logger.info("No schema definition found for module {}: location {}", module.getName(), location);
            }
        }
    }

    private Map<String, Map<String, EntityRawRecords>> loadModuleRecords(Collection<Module> modules) throws IOException, SyntaxError {
        Map<String, Map<String, EntityRawRecords>> schemaRawRecords = new HashMap<>();
        for (Module module : modules) {
            String location = module.getSchemaRecordResourceLocation();
            Resource resource = applicationContext.getResource(location);
            Map<String, EntityRawRecords> allRecords = new HashMap<>();
            if (resource.exists()) {
                logger.info("Found module records definition found for module {}: location {}", module.getName(), location);
                InputStream schemaRecordStream = resource.getInputStream();
                allRecords = RecordParser.parse(new InputStreamReader(schemaRecordStream));
            } else {
                logger.info("No module records definition found for module {}: location {}", module.getName(), location);
            }
            RawSchema rawSchema = schemas.get(module);
            for (RawEntity rawEntity : rawSchema.getRawEntities()) {
                String entityName = rawEntity.getName().toLowerCase();
                String capitalizedEntityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
                String entityLocation = module.getEntityRecordResourceLocation(capitalizedEntityName);
                Resource entityResource = applicationContext.getResource(entityLocation);
                if (entityResource.exists()) {
                    logger.info("Found entity records definition found for module/entity {}/{}: location {}", module.getName(), capitalizedEntityName, entityLocation);
                    InputStream entityRecordStream = entityResource.getInputStream();
                    Map<String, EntityRawRecords> records = RecordParser.parse(new InputStreamReader(entityRecordStream));
                    mergeModuleRecordsWithSpecificEntityRecords(allRecords, records);
                } else {
                    logger.info("No entity records definition found for module/entity {}/{}: location {}", module.getName(), capitalizedEntityName, entityLocation);
                }
            }
            schemaRawRecords.put(module.getName(), allRecords);
        }
        return schemaRawRecords;
    }

    private void mergeModuleRecordsWithSpecificEntityRecords(Map<String, EntityRawRecords> allRecords, Map<String, EntityRawRecords> records) {
        Set<String> allRecordsKeySet = allRecords.keySet();
        Set<String> recordsKeyset = records.keySet();
        HashSet<String> keyset = new HashSet<>();
        keyset.addAll(allRecordsKeySet);
        keyset.addAll(recordsKeyset);
        for (String entity : keyset) {
            EntityRawRecords moduleDefinitions = allRecords.get(entity);
            EntityRawRecords entityDefinitions = records.get(entity);
            if (moduleDefinitions == null && entityDefinitions != null) {
                // simply need to add the specific definitions
                allRecords.put(entity, entityDefinitions);
            }
            if (moduleDefinitions != null && entityDefinitions != null) {
                // NEED to merge here
                EntityRawRecordBuilder mergedBuilder = new EntityRawRecordBuilder(entity);
                mergedBuilder.setDefaultRecord(entityDefinitions.getDef() == null ? moduleDefinitions.getDef() : entityDefinitions.getDef());
                Map<String, EntityRawRecords.VersionedRecords> moduleRecs = moduleDefinitions.getVersionedRecords();
                Map<String, EntityRawRecords.VersionedRecords> entityRecs = entityDefinitions.getVersionedRecords();
                mergedBuilder.addRecord(moduleRecs.values());
                mergedBuilder.addRecord(entityRecs.values());
                allRecords.put(entity, mergedBuilder.build());
            }
            // NO OPERATION for this type of check
            // if( moduleDefinitions != null && entityDefinitions == null)
        }
    }

    private void checkSchemaAndCreateEntitiesCoreObj(Map<String, Map<String, EntityRawRecords>> schemaRawRecordsMap) {
        /* TODO entità estendibii per modulo
            caricare ogni modulo a se stante.. con le entità.. poi fare il merge delle
            entries (ognuna contenente il modulo da dove viene)
            ... vale anche per i record
        */

        // first of all get all the interfaces and entities (to resolve dependencies without ordering)
        Map<String, EntityBuilder> interfaceBuilders = new HashMap<>();
        Map<String, EntityBuilder> entityBuilders = new HashMap<>();
        iterateThroughtRawInterfaces(
                (Module module, RawEntity rawEntityInterface) -> {
                    EntityBuilder entityB = new EntityBuilder(rawEntityInterface, module);
                    String entityName = entityB.getName();
                    if (interfaceBuilders.keySet().contains(entityName)) {
                        throw new DuplicateInterfaceException(entityName);
                    }
                    interfaceBuilders.put(entityName, entityB);
                });
        iterateThroughtRawEntitySchemas((Module module, RawEntity rawEntity) -> {
            EntityBuilder entityB = new EntityBuilder(rawEntity, module);
            String entityName = entityB.getName();
            if (entityBuilders.keySet().contains(entityName)) {
                throw new DuplicateEntityException(entityName);
            }

            // for entities we have also records
            Map<String, EntityRawRecords> rawRecordsByEntity = schemaRawRecordsMap.getOrDefault(module.getName(), Map.of());
            EntityRawRecords entityRawRecords = rawRecordsByEntity.get(entityName.toUpperCase());
            if (entityRawRecords != null) {
                Object defRecord = entityRawRecords.getDef();
                entityB.setDefaultRecord(defRecord);
            }
            entityBuilders.put(entityName, entityB);
        });

        // now we can resolve entity fields
        for (Map.Entry<String, EntityBuilder> entityEntry : entityBuilders.entrySet()) {
            EntityBuilder entityB = entityEntry.getValue();

            // add the meta information to the current entity
            // NB: (CORE_META must be found... otherwise its ok to have a null runtime excp)
            for (RawEntity.Entry entry : interfaceBuilders.get(Entity.CORE_META).getRawEntity().getEntries()) {
                checkAndSetType(entityBuilders, entityB, entry, EntityField.Scope.META);
            }

            // merging Gemini interface if found
            for (String implementsInteface : entityB.getRawEntity().getImplementsIntefaces()) {
                // entity implements a common specification
                EntityBuilder enitityImplementsInterface = interfaceBuilders.get(implementsInteface.toUpperCase());
                for (RawEntity.Entry entry : enitityImplementsInterface.getRawEntity().getEntries()) {
                    checkAndSetType(entityBuilders, entityB, entry, EntityField.Scope.DATA);
                }
            }

            // continue with Gemini entity entries
            for (RawEntity.Entry entry : entityB.getRawEntity().getEntries()) {
                checkAndSetType(entityBuilders, entityB, entry, EntityField.Scope.DATA);
            }
        }

        // now we can build the final entity
        Map<String, Entity> entities = new LinkedHashMap<>();
        for (EntityBuilder entityBuilder : entityBuilders.values()) {
            Entity entity = entityBuilder.build();
            entities.put(entityBuilder.getName(), entity);
        }

        // TODO CHECK embeded fields should not be logicalKey
        this.entities = entities;
    }

    private void checkAndSetType(Map<String, EntityBuilder> entityBuilders, EntityBuilder entityBuilder, RawEntity.Entry entry, EntityField.Scope scope) throws TypeNotFoundException {
        String type = entry.getType().toUpperCase();

        Optional<FieldType> fieldType = FieldType.of(type);
        if (!fieldType.isPresent()) {
            // it is not a reconducible 1 to 1 type

            // try to get an alias
            Optional<FieldType> aliasOfType = FieldType.getAliasOfType(type);
            if (aliasOfType.isPresent()) {
                entityBuilder.addField(aliasOfType.get(), entry, scope);
                return;
            }

            // try to get a static reference for entity
            EntityBuilder entityForType = entityBuilders.get(type);
            if (entityForType != null) {
                boolean embedable = entityForType.getRawEntity().isEmbedable();
                entityBuilder.addField(embedable ? ENTITY_EMBEDED : ENTITY_REF, entry, entityForType.getName(), scope);
                return;
            }

            // try to get an array of entity ref (NB arrays of basic types are handled with aliases)
            if (type.charAt(0) == '[' && type.charAt(type.length() - 1) == ']') {
                String entityRef = type.substring(1, type.length() - 1);
                EntityBuilder entityForRefType = entityBuilders.get(entityRef);
                if (entityForRefType != null) {
                    // TODO handle embedable entity ref
                    boolean embedable = entityForRefType.getRawEntity().isEmbedable();
                    if (!embedable) {
                        entityBuilder.addField(ENTITY_REF_ARRAY, entry, entityForRefType.getName(), scope);
                        return;
                    }
                }
            }

            throw new FieldTypeNotKnown(entityBuilder.getName(), type, entry);
        } else {
            entityBuilder.addField(fieldType.get(), entry, scope);
        }
    }

    /*
    private boolean handleEntityCollectionRef(Map<String, EntityBuilder> entityBuilders, EntityBuilder currentEntityBuilder, RawEntity.Entry entry) {
        String type = entry.getType();
        if (type.startsWith("[") && type.endsWith("]")) {
            String innerType = type.substring(1, type.length() - 1);
            String[] splitted = innerType.split(":");
            if (splitted.length != 2) {
                return false;
            }
            String collectionEntityName = splitted[0];
            String collectionEntityField = splitted[1];
            EntityBuilder collectionEntityBuilder = entityBuilders.get(collectionEntityName);
            if (collectionEntityBuilder == null) {
                return false;
            }
            List<RawEntity.Entry> targetEntityEntries = collectionEntityBuilder.getRawEntity().getEntries();
            Optional<RawEntity.Entry> findLinker = targetEntityEntries.stream()
                    .filter(e -> e.getName().equalsIgnoreCase(collectionEntityField) && e.getType().equalsIgnoreCase(currentEntityBuilder.getName()))
                    .findAny();
            if (findLinker.isPresent()) {
                currentEntityBuilder.addField(ENTITY_COLLECTION_REF, entry, collectionEntityName, collectionEntityField);
                return true;
            }
        }
        return false;
    } */


    /* TODO runtime entity handler
    @Override
    public synchronized void addNewRuntimeEntity(Entity newEntity, Transaction transaction) throws GeminiException {
        Module module = newEntity.getModule();
        assert module.editable();
        persistenceSchemaManager.handleSchemaStorage(transaction, newEntity);
        saveOrUpdateEntityInSchemaFile(newEntity);
    }

    @Override
    public synchronized void addNewRuntimeEntityField(EntityField newEntityField, Transaction transaction) throws GeminiException {
        Entity entity = newEntityField.getEntity();
        assert entity.getModule().editable();
        entity.addField(newEntityField);
        persistenceSchemaManager.handleSchemaStorage(transaction, entity);
        saveOrUpdateEntityInSchemaFile(entity);
    }

    @Override
    public void deleteRuntimeEntity(Entity entity, Transaction transaction) throws GeminiException {
        Module module = entity.getModule();
        assert module.editable();
        persistenceSchemaManager.handleSchemaStorage(transaction, entity);
        saveOrUpdateEntityInSchemaFile(entity);
    }

    @Override
    public void deleteRuntimeEntityField(EntityField field, Transaction transaction) throws GeminiException {
        Entity entity = field.getEntity();
        Module module = entity.getModule();
        assert module.editable();
        entity.removeField(field);
        persistenceSchemaManager.handleSchemaStorage(transaction, entity);
        saveOrUpdateEntityInSchemaFile(entity);
    }


    */

    /*
    private void saveOrUpdateEntityInSchemaFile(Entity entity) throws UnableToUpdateSchemaFIle {
        Module module = entity.getModule();
        RawSchema rawSchema = schemas.get(module);
        RuntimeModule rtm = RuntimeModule.class.cast(module);
        List<RawEntity.Entry> entries = entity.getDataEntityFields().stream().map(ef -> new RawEntity.Entry(ef.getType().name(), ef.getName(), ef.isLogicalKey())).collect(toList());
        rawSchema.addOrUpdateRawEntity(new RawEntity(entity.getName(), false , entries, Collections.EMPTY_LIST));
        entities.putIfAbsent(entity.getName(), entity);
        try {
            rawSchema.persist(rtm.getSchemaLocation());
        } catch (IOException e) {
            throw new UnableToUpdateSchemaFIle();
        }


    } */
}
