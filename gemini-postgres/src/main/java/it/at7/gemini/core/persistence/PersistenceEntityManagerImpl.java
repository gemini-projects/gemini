package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.*;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static it.at7.gemini.core.FieldConverters.logicalKeyFromObject;
import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.entityType;
import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.oneToOneType;

@Service
public class PersistenceEntityManagerImpl implements PersistenceEntityManager {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceEntityManagerImpl.class);

    private FilterVisitor filterVisitor;

    @Autowired
    public PersistenceEntityManagerImpl(FilterVisitor filterVisitor) {
        this.filterVisitor = filterVisitor;
    }

    @Override
    public Optional<EntityRecord> getEntityRecordByLogicalKey(EntityRecord record, Transaction transaction) throws GeminiException {
        Set<EntityRecord.EntityFieldValue> logicalKeyValues = record.getLogicalKeyValue();
        return getRecordByLogicalKeyInner(transaction, record.getEntity(), logicalKeyValues);
    }

    @Override
    public Optional<EntityRecord> getEntityRecordByLogicalKey(Entity entity, Collection<? extends DynamicRecord.FieldValue> logicalKeyValues, Transaction transaction) throws GeminiException {
        return getRecordByLogicalKeyInner(transaction, entity, logicalKeyValues);
    }

    @Override
    public EntityRecord createNewEntityRecord(EntityRecord record, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        QueryWithParams queryWithParams = makeInsertQuery(record, transaction);
        try {
            long recordId = transactionImpl.executeInsert(queryWithParams.sql, queryWithParams.params);
            Optional<EntityRecord> insertedRecord = getEntityRecordById(record.getEntity(), recordId, transactionImpl);
            checkInsertedRecord(recordId, record, insertedRecord);
            return insertedRecord.get();
        } catch (SQLException e) {
            logger.error("createNewEntityRecord SQL Exception", e);
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public EntityRecord updateEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        Object id = record.getID();
        if (id == null) {
            throw IdFieldException.ID_FIELD_REQUIRED("update", record);
        }
        try {
            QueryWithParams queryWithParams = makeModifyQueryFormID(record, transaction);
            transactionImpl.executeUpdate(queryWithParams.sql, queryWithParams.params);
            Optional<EntityRecord> recordById = getEntityRecordById(record.getEntity(), (long) id, transactionImpl);
            assert recordById.isPresent();
            return recordById.get();
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public void deleteEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        Object id = record.getID();
        if (id == null) {
            throw IdFieldException.ID_FIELD_REQUIRED("delete", record);
        }
        try {
            QueryWithParams queryWithParams = makeDeleteQueryByID(record, transaction);
            transactionImpl.executeUpdate(queryWithParams.sql, null);
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public EntityRecord createOrUpdateEntityRecord(EntityRecord entityRecord, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> recordByLogicalKey = getEntityRecordByLogicalKey(entityRecord, transaction);
        if (recordByLogicalKey.isPresent()) {
            EntityRecord persistedEntityRecord = recordByLogicalKey.get();
            if (!sameOf(entityRecord, persistedEntityRecord, transaction)) {
                EntityField idField = entityRecord.getEntity().getIdEntityField();
                Object persistedID = persistedEntityRecord.get(idField);
                entityRecord.put(idField, persistedID);
                persistedEntityRecord = updateEntityRecordByID(entityRecord, transaction);
            }
            return persistedEntityRecord;
        } else {
            return createNewEntityRecord(entityRecord, transaction);
        }
    }

    @Override
    public List<EntityRecord> getEntityRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFielValue, EntityResolutionContext resolutionContext, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        Set<EntityRecord.EntityFieldValue> filter = convertToEntityFieldValues(entity, filterFielValue);
        try {
            QueryWithParams queryWithParams = makeSelectQueryFilteringFiledValue(entity, filter, transaction);
            return transactionImpl.executeQuery(queryWithParams.sql, queryWithParams.params, resultSet -> {
                return fromResultSetToEntityRecord(resultSet, entity, resolutionContext, transaction);
            });
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public List<EntityRecord> getEntityRecordsMatching(Entity entity, FilterContext filterContext, EntityResolutionContext resolutionContext, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        try {
            QueryWithParams query = createSelectQueryFor(entity);
            addFilterRequestTo(query, filterContext, entity);
            return transactionImpl.executeQuery(query.sql, query.params, resultSet -> {
                return fromResultSetToEntityRecord(resultSet, entity, resolutionContext, transaction);
            });
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public Optional<EntityRecord> getEntityRecordById(Entity entity, long recordId, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        try {
            QueryWithParams query = createSelectQueryFor(entity);
            addIdCondition(entity, recordId, query);
            return transactionImpl.executeQuery(query.sql, query.params, resultSet -> {
                List<EntityRecord> entityRecords = fromResultSetToEntityRecord(resultSet, entity, null, transactionImpl);
                assert entityRecords.size() <= 1;
                return entityRecords.size() == 0 ? Optional.empty() : Optional.of(entityRecords.get(0));
            });
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    private void addIdCondition(Entity entity, long recordId, QueryWithParams query) {
        query.sql += String.format("WHERE %s.%s = %d", entity.getName().toUpperCase(), entity.getIdEntityField().getName(), recordId);
    }

    private void addFilterRequestTo(QueryWithParams query, FilterContext filterContext, Entity entity) {
        String sqlFilter = "";
        FilterContext.SearchType searchType = filterContext.getSearchType();
        if (searchType == FilterContext.SearchType.GEMINI) {
            Node rootNode = new RSQLParser().parse(filterContext.getSearchString());
            sqlFilter = rootNode.accept(filterVisitor, entity);
        }
        if (searchType == FilterContext.SearchType.PERSISTENCE) {
            sqlFilter += filterContext.getSearchString();
        }
        query.sql += "WHERE " + sqlFilter;

    }

    private String createSelectQuerySQLFor(Entity entity) {
        String entityName = entity.getName().toUpperCase();
        return String.format("SELECT %1$s.* FROM %1$s ", entityName);
    }

    private QueryWithParams createSelectQueryFor(Entity entity) {
        String select = createSelectQuerySQLFor(entity);
        return new QueryWithParams(select);
    }

    private Set<EntityRecord.EntityFieldValue> convertToEntityFieldValues(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFielValueType) throws EntityFieldException {
        Set<EntityRecord.EntityFieldValue> filter = new HashSet<>();
        for (DynamicRecord.FieldValue fieldValue : filterFielValueType) {
            if (fieldValue instanceof EntityRecord.EntityFieldValue) {
                EntityRecord.EntityFieldValue ef = (EntityRecord.EntityFieldValue) fieldValue;
                filter.add(ef);
            } else {
                filter.add(EntityRecord.EntityFieldValue.create(entity, fieldValue));
            }
        }
        return filter;
    }


    private void checkInsertedRecord(long recordId, EntityRecord record, Optional<EntityRecord> insertedRecord) throws EntityRecordException {
        if (!insertedRecord.isPresent()) {
            throw EntityRecordException.INSERTED_RECORD_NOT_FOUND(record.getEntity(), record.getLogicalKeyValue());
        }
        EntityRecord ins = insertedRecord.get();
        long idField = ins.get(ins.getEntity().getIdEntityField());
        assert recordId == idField;
        // TODO check values is not easy... for Entity Ref and recursive logical keys
        /* for (DynamicRecord.FieldValue fvt : record.getFieldValues()) {
            Field field = fvt.get();
            Object value = fvt.getValue();
            Object persistedValue = ins.get(field);
            --> here we have some problems withGeminiSearchString entityReferences assert value.equals(persistedValue);
        } */
    }

    private boolean sameOf(EntityRecord entityRecord, EntityRecord persistedEntityRecord, Transaction transaction) throws GeminiException {
        for (EntityRecord.EntityFieldValue fieldValue : entityRecord.getOnlyModifiedEntityFieldValue()) {
            EntityField field = fieldValue.getEntityField();
            if (fieldCanBeIgnoredInSameOf(field))
                continue;

            // todo stiamo razzando via la roba non voluta

            Object valueRec = fromFieldToPrimitiveValue(fieldValue, /* TODO  */ Map.of(), transaction);
            EntityRecord.EntityFieldValue persistedFVT = persistedEntityRecord.getEntityFieldValue(field);
            Object persistedValue = fromFieldToPrimitiveValue(persistedFVT, /* TODO  */ Map.of(), transaction);
            if (!((valueRec == null && persistedValue == null) || valueRec.equals(persistedValue))) {
                return false;
            }
        }
        return true;
    }

    private boolean fieldCanBeIgnoredInSameOf(Field field) {
        /* if (field.getType() == FieldType.ENTITY_COLLECTION_REF)
            return true; */
        return false;
    }

    private Optional<EntityRecord> getRecordByLogicalKeyInner(Transaction transaction, Entity entity, Collection<? extends DynamicRecord.FieldValue> logicalKeyValues) throws GeminiException {
        if (logicalKeyValues.isEmpty()) {
            return Optional.empty();
        }
        List<EntityRecord> recordsMatching = getEntityRecordsMatching(entity, logicalKeyValues, transaction);
        int size = recordsMatching.size();
        Assert.isTrue(recordsMatching.isEmpty() || size == 1, String.format("Logical Key must have 0 or 1 records found %s - Database is not consistent withGeminiSearchString schema", size));
        return size == 0 ? Optional.empty() : Optional.of(recordsMatching.get(0));
    }

    private List<EntityRecord> fromResultSetToEntityRecord(ResultSet rs, Entity entity, EntityResolutionContext resolutionContext, Transaction transaction) throws SQLException, GeminiException {
        List<EntityRecord> ret = new ArrayList<>();
        while (rs.next()) {
            EntityRecord er = new EntityRecord(entity);
            for (EntityField field : entity.getSchemaEntityFields()) {
                FieldType type = field.getType();
                String fieldName = field.getName().toLowerCase();
                boolean handled = false;
                if (oneToOneType(type)) {
                    Class specificType = typeClass(type);
                    Object object;
                    if (specificType != null && !specificType.isArray()) {
                        object = rs.getObject(fieldName, specificType);
                    } else if (specificType != null && specificType.isArray()) {
                        Array array = rs.getArray(fieldName);
                        assert array != null;
                        object = specificType.cast(array.getArray());
                    } else {
                        // try the default resolution
                        object = rs.getObject(fieldName);
                    }
                    er.put(field, object == null ? handleNullValueForField(field.getType()) : object);
                    handled = true;
                }
                if (type.equals(FieldType.ENTITY_REF)) {
                    EntityReferenceRecord entityReferenceRecord = null;
                    Object pkValue = rs.getObject(fieldName);
                    if (pkValue != null && (Number.class.isAssignableFrom(pkValue.getClass()) && ((Long) pkValue) != 0)) {
                        entityReferenceRecord = new EntityReferenceRecord(field.getEntityRef());
                        entityReferenceRecord.addPKValue(pkValue);
                        EntityRecord lkEntityRecord = getEntityRecordByPersistedID(transaction, field, pkValue);

                        // TODO put the entity instead of the entityreference ??
                        for (Field entityLkField : field.getEntityRef().getLogicalKey().getLogicalKeyList()) {
                            entityReferenceRecord.addLogicalKeyValue(entityLkField, lkEntityRecord.get(entityLkField));
                        }
                    }
                    er.put(field, entityReferenceRecord);
                    handled = true;
                }
                if (type.equals(FieldType.ENTITY_EMBEDED)) {
                    Object pkValue = rs.getObject(fieldName);
                    EntityRecord recordEmbeded = null;
                    if (pkValue != null && (Number.class.isAssignableFrom(pkValue.getClass()) && ((Long) pkValue) != 0)) {
                        recordEmbeded = getEntityRecordByPersistedID(transaction, field, pkValue);
                    }
                    er.put(field, recordEmbeded);
                    handled = true;
                }
                /* if (type.equals(FieldType.ENTITY_COLLECTION_REF)) {
                    if (resolutionContext.getCollection().equals(EntityResolutionContext.Strategy.NONE)) {
                        handled = true;
                    } else {
                        throw new RuntimeException(String.format("Field %s of type %s not handled - strategy must be implemented", field.getName(), field.getType()));
                    }
                } */
                if (!handled) {
                    throw new RuntimeException(String.format("Field %s of type %s not handled", field.getName(), field.getType()));
                }
            }
            er.put(entity.getIdEntityField(), rs.getLong(entity.getIdEntityField().getName()));
            ret.add(er);
        }
        return ret;
    }

    private EntityRecord getEntityRecordByPersistedID(Transaction transaction, EntityField field, Object pkValue) throws GeminiException {
        Entity entityRef = field.getEntityRef();
        DynamicRecord.FieldValue fieldValue = EntityRecord.EntityFieldValue.create(entityRef.getIdEntityField(), pkValue);
        List<EntityRecord> recordsMatching = getEntityRecordsMatching(entityRef, Set.of(fieldValue), transaction);
        assert recordsMatching.size() == 1;
        return recordsMatching.get(0);
    }

    private QueryWithParams makeInsertQuery(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();

        Map<EntityField, EntityRecord> embededEntityRecords = checkAndCreateEmbededEntity(record, transaction);

        String sql = String.format("INSERT INTO %s", entity.getName().toLowerCase());
        Map<String, Object> params = new HashMap<>();
        List<? extends DynamicRecord.FieldValue> sortedFields = sortFields(record.getAllSchemaEntityFieldValues());
        boolean first = true;
        // TODO Unsupported ope
        for (DynamicRecord.FieldValue field : sortedFields) {
            FieldType type = field.getField().getType();
            if (oneToOneType(type) || entityType(type)) {
                sql += first ? "(" : ",";
                first = false;
                sql += field.getField().getName().toLowerCase();
            }
        }
        sql += ") VALUES ";
        first = true;
        for (DynamicRecord.FieldValue field : sortedFields) {
            FieldType type = field.getField().getType();
            if (oneToOneType(type) || entityType(type)) {
                String columnName = field.getField().getName().toLowerCase();
                sql += first ? "(" : ",";
                first = false;
                sql += ":" + columnName;
                params.put(columnName, fromFieldToPrimitiveValue(field, embededEntityRecords, transaction));
            }
        }
        sql += ")";
        return new QueryWithParams(sql, params);
    }

    private Map<EntityField, EntityRecord> checkAndCreateEmbededEntity(EntityRecord record, Transaction transaction) throws GeminiException {
        Map<EntityField, EntityRecord> results = new HashMap<>();
        for (EntityField entityField : record.getEntityFields()) {
            if (entityField.getType().equals(FieldType.ENTITY_EMBEDED)) {
                EntityRecord embededRec = record.get(entityField);
                embededRec = createNewEntityRecord(embededRec, transaction);
                results.put(entityField, embededRec);
            }
        }
        return results;
    }

    private QueryWithParams makeModifyQueryFormID(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();
        Map<EntityField, EntityRecord> embededEntityRecords = checkAndModifyEmbededEntyRecords(record, transaction);
        String sql = String.format("UPDATE %s SET ", entity.getName().toLowerCase());
        Map<String, Object> params = new HashMap<>();
        List<? extends DynamicRecord.FieldValue> sortedFields = sortFields(record.getOnlyModifiedEntityFieldValue());
        for (int i = 0; i < sortedFields.size(); i++) {
            DynamicRecord.FieldValue field = sortedFields.get(i);
            String columnName = field.getField().getName().toLowerCase();
            FieldType type = field.getField().getType();
            if (oneToOneType(type) || entityType(type)) {
                sql += String.format(" %s = :%s", columnName, columnName);
                sql += i == sortedFields.size() - 1 ? " " : " , ";
                params.put(columnName, fromFieldToPrimitiveValue(field, embededEntityRecords, transaction));
            }
        }
        sql += String.format(" WHERE %s = %s", Field.ID_NAME, record.get(record.getEntity().getIdEntityField(), Long.class));
        return new QueryWithParams(sql, params);
    }

    private Map<EntityField, EntityRecord> checkAndModifyEmbededEntyRecords(EntityRecord record, Transaction transaction) throws GeminiException {
        Map<EntityField, EntityRecord> results = new HashMap<>();
        for (EntityField entityField : record.getEntityFields()) {
            if (entityField.getType().equals(FieldType.ENTITY_EMBEDED)) {
                EntityRecord embededRec = record.get(entityField);
                if(embededRec.getID() != null) {
                    embededRec = updateEntityRecordByID(embededRec, transaction);
                } else {
                    embededRec = createNewEntityRecord(embededRec, transaction);
                }
                results.put(entityField, embededRec);
            }
        }
        return results;
    }

    private QueryWithParams makeSelectQueryFilteringFiledValue(Entity entity, Set<EntityRecord.EntityFieldValue> filterValues, Transaction transaction) throws SQLException, GeminiException {
        String entityName = entity.getName().toLowerCase();
        String sql = String.format("SELECT %1$s.* FROM %1$s WHERE ", entityName);
        Map<String, Object> params = new HashMap<>();
        boolean needAnd = false;
        for (DynamicRecord.FieldValue filterValue : filterValues) {
            Field field = filterValue.getField();
            FieldType type = field.getType();
            sql += needAnd ? "AND" : "";
            if (oneToOneType(type) || type.equals(FieldType.ENTITY_REF)) {
                sql += handleSingleColumnSelectBasicType(entityName, params, filterValue, transaction);
            }
            needAnd = true;
        }
        return new QueryWithParams(sql, params);
    }

    private String handleSingleColumnSelectBasicType(String entityName, Map<String, Object> params, DynamicRecord.FieldValue fieldValue, Transaction transaction) throws SQLException, GeminiException {
        String colName = fieldValue.getField().getName();
        Object value = fromFieldToPrimitiveValue(fieldValue, /* TODO  */ Map.of(), transaction);
        String res = String.format(" %s.%s = :%2$s ", entityName, colName);
        params.put(colName, value);
        return res;
    }

    private QueryWithParams makeDeleteQueryByID(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();
        checkAndDeleteEmbededEntity(record, transaction);
        String sql = String.format("DELETE FROM %s WHERE %s = %s", entity.getName().toLowerCase(), Field.ID_NAME, record.get(record.getEntity().getIdEntityField(), Long.class));
        return new QueryWithParams(sql, null);
    }

    private void checkAndDeleteEmbededEntity(EntityRecord record, Transaction transaction) throws GeminiException {
        for (EntityField entityField : record.getEntityFields()) {
            if (entityField.getType().equals(FieldType.ENTITY_EMBEDED)) {
                EntityRecord embededRec = record.get(entityField);
                deleteEntityRecordByID(embededRec, transaction);
            }
        }
    }


    private Object fromFieldToPrimitiveValue(DynamicRecord.FieldValue fieldValue, Map<EntityField, EntityRecord> embededEntityRecords, Transaction transaction) throws GeminiException {
        Object value = fieldValue.getValue();
        Field field = fieldValue.getField();
        FieldType type = field.getType();
        if (value == null) {
            return handleNullValueForField(type);
        }
        if (oneToOneType(type)) {
            return value;
        }
        if (type.equals(FieldType.ENTITY_REF)) {
            EntityReferenceRecord refRecord;
            if (!EntityReferenceRecord.class.isAssignableFrom(value.getClass())) {
                refRecord = logicalKeyFromObject(field.getEntityRef(), value);
            } else {
                refRecord = (EntityReferenceRecord) value;
            }
            if (refRecord.hasPrimaryKey()) {
                return refRecord.getPrimaryKey();
            } else {
                if (refRecord.hasLogicalKey()) {
                    DynamicRecord lkValue = refRecord.getLogicalKeyRecord();
                    Entity entityRef = field.getEntityRef();
                    Set<DynamicRecord.FieldValue> lkFieldValuesType = lkValue.getFieldValues(entityRef.getLogicalKey().getLogicalKeySet());

                    List<EntityRecord> lkRecords = getEntityRecordsMatching(entityRef, lkFieldValuesType, transaction);
                    if (lkRecords.isEmpty()) {
                        throw EntityRecordException.LK_NOTFOUND(entityRef, lkFieldValuesType);
                    }
                    if (lkRecords.size() != 1) {
                        throw EntityRecordException.MULTIPLE_LK_FOUND(refRecord);
                    }
                    EntityRecord entityRecord = lkRecords.get(0);
                    return entityRecord.get(entityRef.getIdEntityField());
                }
                assert refRecord.equals(EntityReferenceRecord.NO_REFERENCE);
                return 0L;
            }
        }
        if (type.equals(FieldType.ENTITY_EMBEDED)) {
            EntityRecord embededEntity = embededEntityRecords.get(field);
            return embededEntity.get(embededEntity.getEntity().getIdEntityField());
        }
        throw new RuntimeException(String.format("Not implemented %s", field.getType()));
    }

    private Object handleNullValueForField(FieldType type) {
        switch (type) {
            case NUMBER:
                return 0;
            case PK:
                return 0;
            case TEXT:
            case TRANSL_TEXT:
                return "";
            case LONG:
                return 0L;
            case DOUBLE:
                return 0.;
            case BOOL:
                return false;
            case TIME:
            case DATE:
            case DATETIME:
                return null;
            case ENTITY_REF:
                return 0;
            case TEXT_ARRAY:
                return new String[]{};
            case ENTITY_EMBEDED:
                return 0;
            case RECORD:
                break;
        }
        throw new RuntimeException(String.format("NO Null Value for type %s", type.name()));
    }

    private Class typeClass(FieldType type) {
        switch (type) {
            case NUMBER:
                return null;
            case PK:
                return BigInteger.class;
            case TEXT:
            case TRANSL_TEXT:
                return String.class;
            case LONG:
                return Long.class;
            case DOUBLE:
                return Double.class;
            case BOOL:
                return Boolean.class;
            case TIME:
                return LocalTime.class;
            case DATE:
                return LocalDate.class;
            case DATETIME:
                return LocalDateTime.class;
            case ENTITY_REF:
                return BigInteger.class;
            case TEXT_ARRAY:
                return String[].class;
            case RECORD:
                break;
        }
        throw new RuntimeException(String.format("No Class for type %s", type.name()));
    }

    private <T extends DynamicRecord.FieldValue> List<T> sortFields(Collection<T> fieldValue) {
        return fieldValue.stream().sorted(Comparator.comparing(f -> f.getField().getName())).collect(Collectors.toList());
    }

    class QueryWithParams {
        String sql;
        Map<String, Object> params;

        public QueryWithParams(String sql) {
            this.sql = sql;
            this.params = new HashMap<>();
        }

        public QueryWithParams(String sql, Map<String, Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
