package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import it.at7.gemini.core.*;
import it.at7.gemini.exceptions.*;
import it.at7.gemini.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
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
        record.setUUID(getUUIDforEntityRecord(record));
        QueryWithParams queryWithParams = makeInsertQuery(record, transaction);
        try {
            long recordId = transactionImpl.executeInsert(queryWithParams.getSql(), queryWithParams.getParams());
            Optional<EntityRecord> insertedRecord = getEntityRecordById(record.getEntity(), recordId, transactionImpl);
            checkInsertedRecord(recordId, record, insertedRecord);
            return insertedRecord.get();
        } catch (GeminiException e) {
            logger.error("createNewEntityRecord SQL Exception", e);
            throw e;
        }
    }

    @Override
    public EntityRecord updateEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        Object id = record.getID();
        if (id == null) {
            throw IdFieldException.ID_FIELD_REQUIRED("update", record);
        }
        record.setUUID(getUUIDforEntityRecord(record));
        QueryWithParams queryWithParams = makeModifyQueryFromID(record, transaction);
        transactionImpl.executeUpdate(queryWithParams.getSql(), queryWithParams.getParams());
        Optional<EntityRecord> recordById = getEntityRecordById(record.getEntity(), (long) id, transactionImpl);
        assert recordById.isPresent();
        return recordById.get();
    }

    @Override
    public void deleteEntityRecordByID(EntityRecord record, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        Object id = record.getID();
        if (id == null) {
            throw IdFieldException.ID_FIELD_REQUIRED("delete", record);
        }
        QueryWithParams queryWithParams = makeDeleteQueryByID(record, transaction);
        transactionImpl.executeUpdate(queryWithParams.getSql(), null);
    }

    @Override
    public EntityRecord createOrUpdateEntityRecord(EntityRecord entityRecord, Transaction transaction) throws GeminiException {
        Optional<EntityRecord> recordByLogicalKey = getEntityRecordByLogicalKey(entityRecord, transaction);
        if (recordByLogicalKey.isPresent()) {
            EntityRecord persistedEntityRecord = recordByLogicalKey.get();
            // TODO check modified
            if (!sameOf(entityRecord, persistedEntityRecord, transaction)) {
                //    EntityField idField = entityRecord.getEntity().getIdEntityField();
                //    Object persistedID = persistedEntityRecord.get(idField);
                setALLpersistenceIDs(entityRecord, persistedEntityRecord);
                //    entityRecord.put(idField, persistedID);
                persistedEntityRecord = updateEntityRecordByID(entityRecord, transaction);
            }
            return persistedEntityRecord;
        } else {
            return createNewEntityRecord(entityRecord, transaction);
        }
    }

    private void setALLpersistenceIDs(EntityRecord entityRecord, EntityRecord persistedRecord) throws EntityFieldException {
        EntityField idField = entityRecord.getEntity().getIdEntityField();
        Object persistedID = persistedRecord.get(idField);
        entityRecord.put(idField, persistedID);
        for (EntityField field : entityRecord.getEntity().getSchemaEntityFields()) {
            if (field.getType().equals(FieldType.ENTITY_EMBEDED) && entityRecord.get(field) != null) {
                setALLpersistenceIDs(entityRecord.get(field), persistedRecord.get(field));
            }
        }
    }

    @Override
    public List<EntityRecord> getEntityRecordsMatching(Entity entity, Collection<? extends DynamicRecord.FieldValue> filterFielValue, EntityResolutionContext resolutionContext, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        Set<EntityRecord.EntityFieldValue> filter = convertToEntityFieldValues(entity, filterFielValue);
        try {
            QueryWithParams queryWithParams = makeSelectQueryFilteringFiledValue(entity, filter, transaction);
            return transactionImpl.executeQuery(queryWithParams.getSql(), queryWithParams.getParams(), resultSet -> {
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
            addFilter(query, filterContext, entity);
            addOrderBy(query, filterContext, entity);
            addLimit(query, filterContext);
            addOffset(query, filterContext);
            return transactionImpl.executeQuery(query.getSql(), query.getParams(), resultSet -> {
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
            return executeOptionalEntityRecordQuery(entity, transactionImpl, query);
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public Optional<EntityRecord> getEntityRecordByUUID(Entity entity, UUID uuid, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        try {
            QueryWithParams query = createSelectQueryFor(entity);
            addUUIDCondition(entity, uuid, query);
            return executeOptionalEntityRecordQuery(entity, transactionImpl, query);
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    private Optional<EntityRecord> executeOptionalEntityRecordQuery(Entity entity, TransactionImpl transactionImpl, QueryWithParams query) throws SQLException, GeminiException {
        return transactionImpl.executeQuery(query.getSql(), query.getParams(), resultSet -> {
            List<EntityRecord> entityRecords = fromResultSetToEntityRecord(resultSet, entity, null, transactionImpl);
            assert entityRecords.size() <= 1;
            return entityRecords.size() == 0 ? Optional.empty() : Optional.of(entityRecords.get(0));
        });
    }

    private void addIdCondition(Entity entity, long recordId, QueryWithParams query) {
        query.addToSql(String.format("WHERE %s.%s = %d", entity.getName().toUpperCase(), entity.getIdEntityField().getName(), recordId));
    }

    private void addUUIDCondition(Entity entity, UUID uuid, QueryWithParams query) {
        query.addToSql(String.format("WHERE %s.%s = '%s'", entity.getName().toUpperCase(), Field.UUID_NAME, uuid.toString()));
    }

    private void addFilter(QueryWithParams query, FilterContext filterContext, Entity entity) {
        String sqlFilter = "";
        FilterContext.FilterType filterType = filterContext.getFilterType();
        if (filterType == FilterContext.FilterType.GEMINI && !filterContext.getSearchString().isEmpty()) {
            Node rootNode = new RSQLParser().parse(filterContext.getSearchString());
            sqlFilter += " WHERE " + rootNode.accept(filterVisitor, entity);
        }
        if (filterType == FilterContext.FilterType.PERSISTENCE) {
            sqlFilter += " WHERE " + filterContext.getSearchString();
        }
        query.addToSql(sqlFilter);
    }

    private void addOrderBy(QueryWithParams query, FilterContext filterContext, Entity entity) {
        String[] orderBy = filterContext.getOrderBy();
        if (orderBy != null && orderBy.length > 0) {
            StringJoiner oby = new StringJoiner(", ");
            for (String obElem : orderBy) {
                if (obElem.charAt(0) == '-') {
                    oby.add(obElem.substring(1) + " DESC");
                } else {
                    oby.add(obElem + " ASC");
                }
            }
            query.addToSql(" ORDER BY " + oby.toString());
        } else {
            // deterministic order by API
            // TODO need order by update
            Entity.LogicalKey logicalKey = entity.getLogicalKey();
            StringJoiner sj = new StringJoiner(", ");
            for (EntityField field : logicalKey.getLogicalKeyList()) {
                sj.add(field.getName());
            }
            if (sj.length() > 0) {
                query.addToSql(" ORDER BY " + sj.toString());
            }
        }
    }

    private void addLimit(QueryWithParams query, FilterContext filterContext) {
        if (filterContext.getLimit() > 0) {
            query.addToSql(String.format(" LIMIT %d", filterContext.getLimit()));
        }
    }

    private void addOffset(QueryWithParams query, FilterContext filterContext) {
        if (filterContext.getStart() > 0) {
            query.addToSql(String.format(" OFFSET %d", filterContext.getStart()));
        }
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
        if ((entityRecord != null && persistedEntityRecord == null) || (entityRecord == null && persistedEntityRecord != null)) {
            return true; // base case
        }

        for (EntityRecord.EntityFieldValue fieldValue : entityRecord.getOnlyModifiedEntityFieldValue()) {
            EntityField field = fieldValue.getEntityField();
            if (fieldCanBeIgnoredInSameOf(field))
                continue;

            if (field.getType().equals(FieldType.ENTITY_EMBEDED)) {
                EntityRecord embE = entityRecord.get(field);
                EntityRecord embPE = persistedEntityRecord.get(field);
                if (!sameOf(embE, embPE, transaction)) {
                    return false;
                }
            } else {

                // todo stiamo razzando via la roba non voluta
                Object valueRec = fromFieldToPrimitiveValue(fieldValue, /* TODO  */ Map.of(), transaction);
                EntityRecord.EntityFieldValue persistedFVT = persistedEntityRecord.getEntityFieldValue(field);
                Object persistedValue = fromFieldToPrimitiveValue(persistedFVT, /* TODO  */ Map.of(), transaction);
                if (!((valueRec == null && persistedValue == null) || valueRec.equals(persistedValue))) {
                    return false;
                }
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
            if (!entity.isEmbedable()) {
                er.setUUID(rs.getObject(Field.UUID_NAME, UUID.class));
            }
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
                if (type.equals(FieldType.ENTITY_REF_ARRAY)) {
                    Array array = rs.getArray(fieldName);
                    if (array == null) {
                        er.put(field, List.<EntityRef>of());
                    } else {
                        ResultSet rsArray = array.getResultSet(); // TODO ENTITY RESOLUTION CONTEXT
                        List<EntityReferenceRecord> erList = new ArrayList<>();
                        while (rsArray.next()) {
                            long entityID = rsArray.getLong(2);// index 2 contains the value (JDBC spec)
                            Optional<EntityRecord> entityRecordById = getEntityRecordById(field.getEntityRef(), entityID, transaction);
                            if (entityRecordById.isPresent()) {
                                erList.add(EntityReferenceRecord.fromEntityRecord(entityRecordById.get()));
                            } else {
                                throw new RuntimeException("TODO -- Critical exception, Inconsistent DB");
                            }
                        }
                        er.put(field, erList);
                    }
                    // TODO query effective resolution
                    handled = true;
                }
                if (!handled) {
                    throw new RuntimeException(String.format("Field %s of type %s not handled", field.getName(), field.getType()));
                }
            }
            er.put(entity.getIdEntityField(), rs.getLong(entity.getIdEntityField().getName()));
            ret.add(er);
        }
        return ret;
    }

    private EntityRecord getEntityRecordByPersistedID(Transaction transaction, EntityField field, Object pkValue) throws
            GeminiException {
        Entity entityRef = field.getEntityRef();
        DynamicRecord.FieldValue fieldValue = EntityRecord.EntityFieldValue.create(entityRef.getIdEntityField(), pkValue);
        List<EntityRecord> recordsMatching = getEntityRecordsMatching(entityRef, Set.of(fieldValue), transaction);
        assert recordsMatching.size() == 1;
        return recordsMatching.get(0);
    }

    private QueryWithParams makeInsertQuery(EntityRecord record, Transaction transaction) throws GeminiException {
        Entity entity = record.getEntity();

        Map<EntityField, EntityRecord> embededEntityRecords = checkAndCreateEmbededEntity(record, transaction);

        StringBuilder sql = new StringBuilder(String.format("INSERT INTO %s", entity.getName().toLowerCase()));
        Map<String, Object> params = new HashMap<>();
        List<? extends DynamicRecord.FieldValue> sortedFields = sortFields(record.getAllSchemaEntityFieldValues());
        boolean first = true;
        if (!entity.isEmbedable()) {
            sql.append("(").append(Field.UUID_NAME);
            first = false;
        }
        for (DynamicRecord.FieldValue field : sortedFields) {
            FieldType type = field.getField().getType();
            if (oneToOneType(type) || entityType(type)) {
                sql.append(first ? "(" : ",");
                first = false;
                sql.append(field.getField().getName().toLowerCase());
            }
        }
        sql.append(") VALUES ");
        first = true;
        if (!entity.isEmbedable()) {
            first = false;
            sql.append("(:").append(Field.UUID_NAME);
            params.put(Field.UUID_NAME, record.getUUID());
        }
        for (DynamicRecord.FieldValue field : sortedFields) {
            FieldType type = field.getField().getType();
            if (oneToOneType(type) || entityType(type)) {
                String columnName = field.getField().getName().toLowerCase();
                sql.append(first ? "(" : ",");
                first = false;
                sql.append(":").append(columnName);
                params.put(columnName, fromFieldToPrimitiveValue(field, embededEntityRecords, transaction));
            }
        }
        sql.append(")");
        return new QueryWithParams(sql.toString(), params);
    }

    private Map<EntityField, EntityRecord> checkAndCreateEmbededEntity(EntityRecord record, Transaction transaction) throws
            GeminiException {
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

    private QueryWithParams makeModifyQueryFromID(EntityRecord record, Transaction transaction) throws
            GeminiException {
        Entity entity = record.getEntity();
        Map<EntityField, EntityRecord> embededEntityRecords = checkAndModifyEmbededEntyRecords(record, transaction);
        StringBuilder sql = new StringBuilder(String.format("UPDATE %s SET ", entity.getName().toLowerCase()));
        Map<String, Object> params = new HashMap<>();
        if (!record.getEntity().isEmbedable() && record.getUUID() != null) { // uuis should be updated only if it is provided
            sql.append(String.format(" %s = :%s , ", Field.UUID_NAME, Field.UUID_NAME));
            params.put(Field.UUID_NAME, record.getUUID());
        }
        List<? extends DynamicRecord.FieldValue> sortedFields = sortFields(record.getOnlyModifiedEntityFieldValue());
        for (int i = 0; i < sortedFields.size(); i++) {
            DynamicRecord.FieldValue field = sortedFields.get(i);
            String columnName = field.getField().getName().toLowerCase();
            FieldType type = field.getField().getType();
            if (oneToOneType(type) || entityType(type)) {
                sql.append(String.format(" %s = :%s", columnName, columnName));
                sql.append(i == sortedFields.size() - 1 ? " " : " , ");
                params.put(columnName, fromFieldToPrimitiveValue(field, embededEntityRecords, transaction));
            }
        }
        sql.append(String.format(" WHERE %s = %s", Field.ID_NAME, record.get(record.getEntity().getIdEntityField(), Long.class)));
        return new QueryWithParams(sql.toString(), params);
    }

    private Map<EntityField, EntityRecord> checkAndModifyEmbededEntyRecords(EntityRecord record, Transaction
            transaction) throws GeminiException {
        Map<EntityField, EntityRecord> results = new HashMap<>();
        for (EntityField entityField : record.getEntityFields()) {
            if (entityField.getType().equals(FieldType.ENTITY_EMBEDED)) {
                EntityRecord embededRec = record.get(entityField);
                assert embededRec != null;
                if (embededRec.getID() != null) {
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
                throw new RuntimeException(String.format("fromFieldToPrimitiveValue %s - EntityRef without Pk or LK", field.getName()));
            }
        }
        if (type.equals(FieldType.ENTITY_EMBEDED)) {
            EntityRecord embededEntity = embededEntityRecords.get(field);
            return embededEntity.get(embededEntity.getEntity().getIdEntityField());
        }
        if (type.equals(FieldType.ENTITY_REF_ARRAY)) {
            if (Collection.class.isAssignableFrom(value.getClass())) {
                Collection<Object> genColl = (Collection) value;
                if (genColl.isEmpty()) {
                    return null;
                }
                Object[] elements = genColl.toArray();
                Object firstElem = elements[0];

                Long[] entityRefIds = new Long[elements.length];
                int i = 0;
                if (EntityRecord.class.isAssignableFrom(firstElem.getClass())) {
                    Collection<EntityRecord> entityRecords = (Collection<EntityRecord>) value;
                    for (EntityRecord entityRecord : entityRecords) {
                        if (entityRecord.getID() != null) {
                            entityRefIds[i++] = (long) entityRecord.getID();
                        } else {
                            Optional<EntityRecord> queryedEntityRec = getEntityRecordByLogicalKey(entityRecord, transaction);
                            if (queryedEntityRec.isPresent()) {
                                entityRefIds[i++] = (long) queryedEntityRec.get().getID();
                            } else {
                                throw EntityRecordException.LK_NOTFOUND(entityRecord.getEntity(), entityRecord.getLogicalKeyValue());
                            }
                        }
                    }
                }

                TransactionImpl tImp = (TransactionImpl) transaction;
                Connection connection = tImp.getConnection();
                try {
                    return connection.createArrayOf("BIGINT", entityRefIds);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new RuntimeException(String.format("fromFieldToPrimitiveValue - Not implemented %s", field.getType()));
    }

    @Override
    public UUID getUUIDforEntityRecord(EntityRecord record) {
        // uuid: EntityName + LogicalKey --> it should be unique
        StringBuilder uuidString = new StringBuilder(record.getEntity().getName());
        Set<EntityRecord.EntityFieldValue> logicalKeyValues = record.getLogicalKeyValue();
        List<EntityRecord.EntityFieldValue> sortedLkValues = logicalKeyValues.stream().sorted(Comparator.comparing(e -> e.getField().getName())).collect(Collectors.toList());
        for (EntityRecord.EntityFieldValue lkValue : sortedLkValues) {
            uuidString.append(fromEntityFieldToUUID(lkValue));
        }
        return UUID.nameUUIDFromBytes(uuidString.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String fromEntityFieldToUUID(EntityRecord.EntityFieldValue lkValue) {
        EntityField field = lkValue.getEntityField();
        FieldType type = field.getType();
        Object value = lkValue.getValue();
        if (value == null) {
            return "null";
        }
        if (oneToOneType(type)) {
            return value.toString();
        }
        if (type.equals(FieldType.ENTITY_REF)) {
            EntityReferenceRecord refRecord;
            Entity entityRef = field.getEntityRef();
            if (!EntityReferenceRecord.class.isAssignableFrom(value.getClass())) {
                refRecord = logicalKeyFromObject(entityRef, value);
            } else {
                refRecord = (EntityReferenceRecord) value;
            }
            if (refRecord.hasLogicalKey()) {
                DynamicRecord logicalKeyRecord = refRecord.getLogicalKeyRecord();
                List<EntityField> lkv = entityRef.getLogicalKey().getLogicalKeyList();
                lkv.sort(Comparator.comparing(Field::getName));
                StringBuilder res = new StringBuilder();
                for (EntityField lkPieceField : lkv) {
                    EntityRecord.EntityFieldValue entityFieldValue = EntityRecord.EntityFieldValue.create(lkPieceField, logicalKeyRecord.getFieldValue(lkPieceField));
                    res.append(fromEntityFieldToUUID(entityFieldValue));
                }
                return res.toString();
            } else {
                logger.warn(String.format("Using primary key to generate the UUID for %s.%s - may be an error", field.getEntity().getName(), field.getName()));
            }
        }
        throw new RuntimeException(String.format("fromEntityFieldToUUID - Not implemented %s", field.getType()));
    }

    private Object handleNullValueForField(FieldType type) {
        switch (type) {
            case NUMBER:
                return 0;
            case PK:
                return 0;
            case TEXT:
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
            case ENTITY_REF_ARRAY:
                return null;
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
        private StringBuilder sqlBuilder;
        private Map<String, Object> params;

        public QueryWithParams(String sql) {
            this.sqlBuilder = new StringBuilder(sql);
            this.params = new HashMap<>();
        }

        public QueryWithParams(String sql, Map<String, Object> params) {
            this.sqlBuilder = new StringBuilder(sql);
            this.params = params;
        }

        public QueryWithParams addToSql(String sql) {
            sqlBuilder.append(sql);
            return this;
        }

        public String getSql() {
            return sqlBuilder.toString();
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }
}
