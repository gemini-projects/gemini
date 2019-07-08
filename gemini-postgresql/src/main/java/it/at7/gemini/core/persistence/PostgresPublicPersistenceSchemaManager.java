package it.at7.gemini.core.persistence;

import it.at7.gemini.core.Module;
import it.at7.gemini.core.Transaction;
import it.at7.gemini.core.TransactionImpl;
import it.at7.gemini.exceptions.GeminiException;
import it.at7.gemini.exceptions.GeminiGenericException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.*;
import static java.util.stream.Collectors.toList;

@Service
public class PostgresPublicPersistenceSchemaManager implements PersistenceSchemaManager {
    private static final String META_PREFIX = "_meta_";
    private static final Logger logger = LoggerFactory.getLogger(PostgresPublicPersistenceSchemaManager.class);

    @Override
    public void beforeLoadSchema(List<Module> modules, Transaction transaction) throws GeminiException {
        /* // TODO on dynamic schema (runtime)
        try {
            TransactionImpl transactionImpl = (TransactionImpl) transaction;
            for (Module module : modules.values()) {
                if (module.editable()) {
                    synchronizeRuntimeModules((RuntimeModule) module, transactionImpl);
                }
            }
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        } */
    }

    @Override
    public void handleSchemaStorage(Transaction transaction, Collection<Entity> entities) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        for (Entity entity : entities) {
            // two cycle.. it is a beforeAll
            invokeCreateEntityStorageBefore(entity, transactionImpl);
        }
        for (Entity entity : entities) {
            handleSingleEntityStorage(transactionImpl, entity, OPE.UPDATE);
        }
    }

    @Override
    public void deleteUnnecessaryEntites(Collection<Entity> entities, Transaction transaction) throws GeminiException {
        try {
            TransactionImpl transactionImpl = (TransactionImpl) transaction;
            // NB - we are using fields that should be existed in entity/fields (generalize ? )
            List<Long> entitiesID = entities.stream().map(Entity::getIDValue).map(Long.class::cast).collect(toList());
            String sql = String.format("SELECT name FROM entity WHERE %s NOT IN (:ids)", Field.ID_NAME);
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("ids", entitiesID);
            transactionImpl.executeQuery(sql, parameters, rs -> {
                while (rs.next()) {
                    String name = rs.getString(1);
                    transactionImpl.executeUpdate(String.format("DROP TABLE IF EXISTS %s", wrapDoubleQuotes(name.toLowerCase())));
                }
            });
            String deleteEntitiesSql = String.format("DELETE FROM entity WHERE %s NOT IN (:ids)", Field.ID_NAME);
            String deleteEntitiesFieldsSql = String.format("DELETE FROM field WHERE entity NOT IN (:ids)");
            transactionImpl.executeUpdate(deleteEntitiesSql, parameters);
            transactionImpl.executeUpdate(deleteEntitiesFieldsSql, parameters);
        } catch (SQLException e) {
            logger.error("deleteUnnecessaryEntites Failed", e);
            throw new GeminiGenericException(e);
        }
    }

    @Override
    public boolean entityStorageExists(Entity entity, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        try {
            return entityStorageExists(entity.getName(), transactionImpl);
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public void deleteUnnecessaryFields(Entity entity, Set<EntityField> fields, Transaction transaction) throws GeminiException {
        try {
            TransactionImpl transactionImpl = (TransactionImpl) transaction;
            List<Long> fieldsID = fields.stream().map(EntityField::getIDValue).map(Long.class::cast).collect(toList());
            if (!fieldsID.isEmpty()) {
                String sql = String.format("SELECT name FROM field WHERE %s NOT IN (:ids) AND entity = :entityId", Field.ID_NAME);
                HashMap<String, Object> parameters = new HashMap<>();
                parameters.put("ids", fieldsID);
                parameters.put("entityId", entity.getIDValue());
                transactionImpl.executeQuery(sql, parameters, rs -> {
                    while (rs.next()) {
                        String columnName = rs.getString(1).toLowerCase();
                        transactionImpl.executeUpdate(String.format("ALTER TABLE %s DROP COLUMN IF EXISTS %s", wrapDoubleQuotes(entity.getName().toLowerCase()), wrapDoubleQuotes(columnName)));
                    }
                });
                String deleteEntitiesFieldsSql = String.format("DELETE FROM field WHERE %s NOT IN (:ids) AND entity = :entityId", Field.ID_NAME);
                transactionImpl.executeUpdate(deleteEntitiesFieldsSql, parameters);
            }
        } catch (SQLException e) {
            throw GeminiGenericException.wrap(e);
        }
    }

    @Override
    public void invokeCreateEntityStorageBefore(Entity entity, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        try {
            checkOrCreatePKDomainForModel(entity.getName(), transactionImpl);
        } catch (SQLException e) {
            logger.error("invokeCreateEntityStorageBefore Failed: for {} of Module {}", entity.getName(), entity.getModule().getName(), e);
            throw new GeminiGenericException(e);
        }
    }

    private void handleSingleEntityStorage(TransactionImpl transaction, Entity entity, OPE operation) throws GeminiException {
        try {
            if (operation.equals(OPE.UPDATE)) {
                if (!entityStorageExists(entity, transaction)) {
                    createEntityStorage(entity, transaction);
                } else {
                    updateEntityStorage(entity, transaction);
                }
            }
            if (operation.equals(OPE.DELETE)) {
                deleteEntityStorage(entity, transaction);
            }
        } catch (SQLException e) {
            logger.error("handleSingleEntityStorage Failed: for {} of Module {}", entity.getName(), entity.getModule().getName(), e);
            throw new GeminiGenericException(e);
        }
    }

    private void deleteEntityStorage(Entity entity, Transaction transaction) throws GeminiException {
        TransactionImpl transactionImpl = (TransactionImpl) transaction;
        transactionImpl.executeUpdate(String.format("DROP TABLE IF EXISTS %s", entity.getName()));
    }


    /*
    private void synchronizeRuntimeModules(RuntimeModule module, TransactionImpl transaction) throws IOException, SQLException, GeminiException {
        String schemaLocation = module.getSchemaLocation();
        File file = new File(schemaLocation);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            parentFile.mkdirs();
            file.createNewFile();
            String name = module.getName();
            if (entityStorageExists("entity", transaction)) {
                String entitySql = String.format("SELECT %s, name FROM entity WHERE module = :name", Field.ID_NAME);
                HashMap<String, Object> params = new HashMap<>();
                params.put("name", name);
                Set<RawEntity> rawEntities = new HashSet<>();
                transaction.executeQuery(entitySql, params, rs -> {
                    while (rs.next()) {
                        long entityId = rs.getLong(Field.ID_NAME);
                        String entityName = rs.getString(EntityRef.FIELDS.NAME);
                        boolean embedable = rs.getBoolean(EntityRef.FIELDS.EMBEDABLE);
                        String fieldSql = String.format("SELECT * FROM field WHERE entity = " + entityId);
                        List<RawEntity.Entry> entires = new ArrayList<>();
                        transaction.executeQuery(fieldSql, rsF -> {
                            while (rsF.next()) {
                                entires.add(new RawEntity.Entry(rsF.getString("type"), rsF.getString("name"), rsF.getBoolean("islogicalkey")));
                            }
                        });
                        rawEntities.add(new RawEntity(entityName, embedable, entires, Collections.EMPTY_LIST)); // TODO interface on runtime
                    }
                });
                RawSchema rawSchema = new RawSchema(rawEntities);
                rawSchema.persist(schemaLocation);
            }
        }
    } */

    private boolean entityStorageExists(String name, TransactionImpl transaction) throws GeminiException, SQLException {
        // we use raw jdbc connection
        String sqlTableExists = "" +
                "   SELECT EXISTS ( " +
                "       SELECT 1" +
                "       FROM   information_schema.tables " +
                "       WHERE  table_schema = :table_schema" +
                "       AND    table_name =  :table_name" +
                "   );";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("table_schema", "public");
        parameters.put("table_name", name.toLowerCase());
        return transaction.executeQuery(sqlTableExists, parameters, this::exists);
    }

    private void createEntityStorage(Entity entity, TransactionImpl transaction) throws GeminiException {
        logger.info("{}: creating Entity {}", entity.getModule().getName(), entity.getName());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE ").append(wrapDoubleQuotes(entity.getName().toLowerCase())).append(" ( ");
        sqlBuilder.append(primaryKeyField(Field.ID_NAME));
        if (!entity.isEmbedable()) {
            sqlBuilder.append(uuidField());
        }
        entity.getMetaEntityFields().forEach(mf -> sqlBuilder.append(", ").append(field(mf)));
        entity.getDataEntityFields().forEach(f -> sqlBuilder.append(", ").append(field(f)));
        handleUniqueLogicalKeyConstraint(sqlBuilder, entity);
        sqlBuilder.append(" );");
        transaction.executeUpdate(sqlBuilder.toString());
        // TODO for runtime is better unique constrain or index ?? check later
        // checkOrCreteLogicalKeyUniqueIndex(entity.getName(), entity.getLogicalKey(), transaction);
    }

    private void updateEntityStorage(Entity entity, TransactionImpl transaction) throws SQLException, GeminiException {
        logger.info("{}: check/update Fields for {}", entity.getModule().getName(), entity.getName());

        /*
            NB meta fields columns are creted or updated always starting from the schema file.
            If you delete the field from the schema, the column isn't removed from the database.
         */
        HashSet<EntityField> updateSet = new HashSet<>(entity.getMetaEntityFields());
        updateSet.addAll(entity.getDataEntityFields());

        for (EntityField field : updateSet) {
            if (field.getType() == FieldType.ENTITY_REF) {
                // to be sure we have
                Entity refEntity = field.getEntityRef();
                assert refEntity != null;
                checkOrCreatePKDomainForModel(refEntity.getName(), transaction);
            }
            checkOrUpdateColumn(entity, field, transaction);
        }
        // TODO update logical key constraint -- use the following query to get columns for logical key constraint
        /* SELECT
        ccu.column_name AS columns
                FROM
        information_schema.table_constraints AS tc
        JOIN information_schema	.constraint_column_usage AS ccu
        ON ccu.constraint_name = tc.constraint_name
        where
        tc.constraint_name = 'fieldresolution_lk' */
    }


    private void handleUniqueLogicalKeyConstraint(StringBuilder sqlBuilder, Entity entity) {
        List<EntityField> logicalKeyList = entity.getLogicalKey().getLogicalKeyList();
        if (logicalKeyList != null && logicalKeyList.size() > 0) {
            String constraintName = entity.getName().toLowerCase() + "_lk";
            sqlBuilder.append(String.format(", CONSTRAINT %s UNIQUE (", wrapDoubleQuotes(constraintName)));
            for (int i = 0; i < logicalKeyList.size(); i++) {
                Field field = logicalKeyList.get(i);
                sqlBuilder.append(fieldUnique(field));
                if (i < logicalKeyList.size() - 1)
                    sqlBuilder.append(", ");
            }
            sqlBuilder.append(")");
        }
    }

    private void checkOrCreatePKDomainForModel(String entityName, TransactionImpl transaction) throws SQLException, GeminiException {
        String sqlDomainExists = "" +
                "   SELECT EXISTS ( " +
                "       SELECT 1" +
                "       FROM   information_schema.domains " +
                "       WHERE  domain_schema = :domain_schema" +
                "       AND    domain_name =  :domain_name" +
                "   );";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("domain_schema", "public");
        parameters.put("domain_name", pkForeignKeyDomainFromEntity(entityName));
        transaction.executeQuery(sqlDomainExists, parameters, resultSet -> {
            if (!exists(resultSet)) {
                createPkDomainForModel(entityName, transaction);
            }
        });
    }

    /*
    private void checkOrCreteLogicalKeyUniqueIndex(String name, Entity.LogicalKey logicalKey, TransactionImpl transaction) throws SQLException, GeminiException {
        String indexName = "LK_Unique_" + name.toLowerCase();
        String sdlIndexExist = "select" +
                "    t.relname as table_name," +
                "    i.relname as index_name," +
                "    a.attname as column_name" +
                " from " +
                "    pg_class t," +
                "    pg_class i," +
                "    pg_index ix," +
                "    pg_attribute a" +
                "  where " +
                "    t.oid = ix.indrelid" +
                "    and i.oid = ix.indexrelid" +
                "    and a.attrelid = t.oid" +
                "    and a.attnum = ANY(ix.indkey)" +
                "    and t.relkind = 'r'" +
                "    and t.relname = :table_name" +
                "    and i.relname = :index_name" +
                "  order by" +
                "    t.relname," +
                "    i.relname;";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("table_name", name.toLowerCase());
        parameters.put("index_name", indexName);
        transaction.executeQuery(sdlIndexExist, parameters, resultSet -> {
            if (resultSet.next()) {
                // TODO handle existing indexes (check or change)
            } else {
                String createUniqueLk = "CREATE UNIQUE INDEX " + indexName + " ON " + name.toLowerCase() + "(";
                List<EntityField> logicalKeyList = logicalKey.getLogicalKeyList();
                for (int i = 0; i < logicalKeyList.size(); i++) {
                    Field field = logicalKeyList.get(i);
                    createUniqueLk += field.getName().toLowerCase();
                    if (i < logicalKeyList.size() - 1) {
                        createUniqueLk += ", ";
                    }
                }
                createUniqueLk += ")";
                transaction.executeUpdate(createUniqueLk);
            }
        });

    } */

    private boolean exists(ResultSet resultSet) throws SQLException {
        resultSet.next();
        return resultSet.getBoolean(1);
    }

    private void createPkDomainForModel(String modelName, TransactionImpl transaction) throws GeminiException {
        String domainSql = String.format(
                "CREATE DOMAIN %s AS %s", pkForeignKeyDomainFromEntity(modelName), "BIGINT");
        transaction.executeUpdate(domainSql);
    }


    private void checkOrUpdateColumn(Entity entity, EntityField field, TransactionImpl transaction) throws SQLException, GeminiException {
        String sqlColumnsCheck = "" +
                "   SELECT *" +
                "   FROM information_schema.columns" +
                "   WHERE table_schema = :schema" +
                "   AND table_name   = :table_name" +
                "   AND column_name = :col_name;";
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("schema", "public");
        parameters.put("table_name", entity.getName().toLowerCase());
        parameters.put("col_name", fieldName(field, false));

        transaction.executeQuery(sqlColumnsCheck, parameters, resultSet -> {
            boolean exists = resultSet.next();
            if (!exists) {
                String fieldSqlType = getSqlPrimitiveType(field);
                logger.info("Table {}: adding column {} {}", entity.getName(), field.getName(), fieldSqlType);
                String sqlAlterTable =
                        "ALTER TABLE " + wrapDoubleQuotes(entity.getName().toLowerCase()) +
                                " ADD COLUMN " + field(field);

                transaction.executeUpdate(sqlAlterTable);
            } else {
                String data_type = resultSet.getString("data_type");
                String domain_name = resultSet.getString("domain_name");
                if (!checkSqlType(field, data_type, domain_name)) {
                    String sqlAlterTable =
                            "ALTER TABLE " + wrapDoubleQuotes(entity.getName().toLowerCase()) +
                                    " DROP COLUMN " + fieldName(field, true);
                    sqlAlterTable += "; " +
                            "ALTER TABLE " + wrapDoubleQuotes(entity.getName().toLowerCase()) +
                            " ADD COLUMN " + field(field);
                    transaction.executeUpdate(sqlAlterTable);
                }
                // TODO Complex types
                // nothing to DO here
            }

        });
    }

    private boolean checkSqlType(Field field, String data_type, String domain_name) {
        FieldType type = field.getType();
        switch (type) {
            case PK:
                return data_type.equals("bigint");
            case TEXT:
                return data_type.equals("text");
            case PASSWORD:
                return data_type.equals("json");
            case NUMBER:
                return data_type.equals("numeric");
            case LONG:
                return data_type.equals("bigint");
            case DOUBLE:
                return data_type.equals("double precision");
            case BOOL:
                return data_type.equals("boolean");
            case TIME:
                return data_type.contains("time");
            case DATE:
                return data_type.contains("date");
            case DATETIME:
                return data_type.contains("timestamp");
            case ENTITY_REF:
            case ENTITY_EMBEDED:
                Entity entityRef = field.getEntityRef();
                assert entityRef != null;
                String name = pkForeignKeyDomainFromEntity(entityRef.getName());
                return data_type.equals("bigint") && name.equals(domain_name);
            case RECORD:
                break;
            case TEXT_ARRAY:
            case ENTITY_REF_ARRAY:
                return data_type.contains("ARRAY");

        }
        throw new RuntimeException(String.format("Field %s not handled for drop/createBearer dirty column", type));
    }

    private String primaryKeyField(String id) {
        return String.format("%s BIGSERIAL PRIMARY KEY", wrapDoubleQuotes(id));
    }

    private String uuidField() {
        return String.format(", %s uuid UNIQUE", wrapDoubleQuotes(Field.UUID_NAME));
    }


    private String field(EntityField field) {
        FieldType type = field.getType();
        if (oneToOneType(type) || entityType(type) || passwordType(type)) {
            // return fieldName(field, true) + (isAlterColumn ? " TYPE " : " ") + getSqlPrimitiveType(field);
            return fieldName(field, true) + " " + getSqlPrimitiveType(field);
        }
        throw new RuntimeException(String.format("%s - Field of type %s Not Implemented", field.getName(), field.getType())); // TODO
    }

    private String fieldUnique(Field field) {
        FieldType type = field.getType();
        if (oneToOneType(type) || type.equals(FieldType.ENTITY_REF)) {
            return wrapDoubleQuotes(field.getName().toLowerCase());
        }
        throw new RuntimeException(String.format("%s - Unique Field Not Implemented", field.getName()));
    }

    private String getSqlPrimitiveType(Field field) {
        switch (field.getType()) {
            case PK:
                break;
            case TEXT:
                return "TEXT";
            case NUMBER:
                return "NUMERIC";
            case LONG:
                return "BIGINT";
            case DOUBLE:
                return "DOUBLE PRECISION";
            case BOOL:
                return "BOOLEAN";
            case TIME:
                return "TIME";
            case DATE:
                return "DATE";
            case DATETIME:
                return "TIMESTAMP";
            case PASSWORD:
                return "JSON";
            case ENTITY_REF:
            case ENTITY_EMBEDED:
                Entity entityRef = field.getEntityRef();
                return pkForeignKeyDomainFromEntity(entityRef.getName()); // it is also a domain
            case TEXT_ARRAY:
                return "TEXT[]";
            case ENTITY_REF_ARRAY:
                return pkDomainArrayFromEntity(field.getEntityRef().getName()); // it is also a domain
            case RECORD:
                throw sqlTypeException(field);
        }
        throw sqlTypeException(field);
    }

    private RuntimeException sqlTypeException(Field field) {
        return new RuntimeException(String.format("FilterType %s for field %s not Assigned to any PostrgresType", field.getType(), field.getName()));
    }

    private enum OPE {
        UPDATE,
        DELETE
    }

    public static String fieldName(EntityField field, boolean wrap) {
        String prefix = field.getScope().equals(EntityField.Scope.META) ? META_PREFIX : "";
        String name = prefix + field.getName().toLowerCase();
        if (wrap)
            return wrapDoubleQuotes(name);
        return name;
    }
}
