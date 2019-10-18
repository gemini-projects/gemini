package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import it.at7.gemini.core.FieldConverters;
import it.at7.gemini.core.persistence.FilterVisitor.FilterVisitorContext;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.Field;
import it.at7.gemini.schema.FieldType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;
import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.fieldName;
import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.wrapDoubleQuotes;
import static it.at7.gemini.core.persistence.FilterVisitor.EMPTY_OPERATOR;
import static it.at7.gemini.core.persistence.FilterVisitor.LIKE_OPERATOR;

public class EntityRefTypeFilterVisitor implements GeminiTypeFilterVisitor {

    private final FilterVisitor parentFilterVisitor;
    Map<ComparisonOperator, String> supportedOperators = Map.of(
            EQUAL, " IN ( %s )",
            NOT_EQUAL, " NOT IN ( %s ) UNION ALL SELECT 0",
            IN, " IN ( %s )",
            NOT_IN, " NOT IN ( %s ) UNION ALL SELECT 0",
            LIKE_OPERATOR, " LIKE %s",
            EMPTY_OPERATOR, " = 0"
    );

    public EntityRefTypeFilterVisitor(FilterVisitor filterVisitor) {
        this.parentFilterVisitor = filterVisitor;
    }

    @Override
    public QueryWithParams visit(EntityField field, ComparisonNode node, FilterVisitorContext filterVisitorContext) {
        ComparisonOperator operator = node.getOperator();
        if (supportedOperators.containsKey(operator)) {
            FieldType type = field.getType();
            assert type.equals(FieldType.ENTITY_REF);
            String sqlOperator = supportedOperators.get(operator);

            if (operator.equals(EMPTY_OPERATOR)) {
                return new QueryWithParams(wrapDoubleQuotes(field.getEntity().getName().toLowerCase()) + "." +
                        fieldName(field, true) + sqlOperator, Map.of());
            }

            Entity entityRef = field.getEntityRef();
            Entity.LogicalKey refLogicalKey = entityRef.getLogicalKey();
            assert refLogicalKey != null;
            QueryWithParams innerINQuery;

            List<String> arguments = node.getArguments();
            try {
                innerINQuery = handleUUID(arguments, sqlOperator, filterVisitorContext, entityRef);
            } catch (IllegalArgumentException e) {
                if (refLogicalKey.getLogicalKeyList().size() == 1) {
                    innerINQuery = handleSingleLogicalKeyEntity(field, node, sqlOperator, filterVisitorContext);
                } else {
                    innerINQuery = handleMultipleLogicalKeyEntities(field, node, sqlOperator, filterVisitorContext);
                }
            }
            String sqlFullString = wrapDoubleQuotes(field.getEntity().getName().toLowerCase()) + "." +
                    fieldName(field, true)
                    + " IN ( " + innerINQuery.getSql() + " )";
            return new QueryWithParams(sqlFullString, innerINQuery.getParams());

        }
        throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor unsupported operator %s", node.getOperator().getSymbol()));
    }

    @NotNull
    private QueryWithParams handleUUID(List<String> arguments, String sqlOperator, FilterVisitorContext filterVisitorContext, Entity entityRef) {
        QueryWithParams innerINQuery;
        List<UUID> uuids = arguments.stream().map(UUID::fromString).collect(Collectors.toList());
        String entityName = wrapDoubleQuotes(entityRef.getName().toLowerCase());
        String idName = wrapDoubleQuotes(entityRef.getIdEntityField().getName().toLowerCase());
        String uuidParameter = filterVisitorContext.parameterFor(Field.UUID_NAME);
        String condition = String.format(sqlOperator, ":" + uuidParameter);
        innerINQuery = new QueryWithParams(String.format("SELECT %1$s.%2$s FROM %1$s WHERE %1$s.%3$s %4$s", entityName, idName, Field.UUID_NAME, condition),
                Map.of(uuidParameter, uuids));
        return innerINQuery;
    }

    private QueryWithParams handleMultipleLogicalKeyEntities(EntityField field, ComparisonNode node, String sqlOperator, FilterVisitorContext filterVisitorContext) {
        List<String> arguments = node.getArguments();
        if (arguments.size() == 1) {
            Entity entityRef = field.getEntityRef();
            String entityName = wrapDoubleQuotes(entityRef.getName().toLowerCase());
            String idName = wrapDoubleQuotes(entityRef.getIdEntityField().getName().toLowerCase());
            String argument = arguments.get(0);

            // we need to parse again the argument if we dont' have a UUID.. since it contains other conditions on keys
            Node rootNode = new RSQLParser().parse(argument);
            QueryWithParams innerQuery = rootNode.accept(this.parentFilterVisitor, FilterVisitorContext.of(entityRef, filterVisitorContext.counterByParameter));
            return new QueryWithParams(String.format("SELECT %1$s.%2$s" +
                    "  FROM %1$s WHERE ", entityName, idName)
                    + innerQuery.getSql(), innerQuery.getParams());
        }
        throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor unsupported operator %s withRecord for that one argument", node.getOperator().getSymbol()));
    }

    private QueryWithParams handleSingleLogicalKeyEntity(EntityField field, ComparisonNode node, String sqlOperator, FilterVisitorContext filterVisitorContext) {
        Entity entityRef = field.getEntityRef();
        Entity.LogicalKey logicalKey = entityRef.getLogicalKey();
        EntityField lkField = logicalKey.getLogicalKeyList().get(0);
        List<String> arguments = node.getArguments();
        String parameterName = filterVisitorContext.parameterFor(fieldName(lkField, false));
        String innerFilter = ":" + parameterName;

        List<Object> parameters = arguments.stream().map(a -> resolveArgumentValue(lkField, a)).collect(Collectors.toList());

        String sqlFilter = String.format("" +
                        "SELECT %1$s.%2$s" +
                        "  FROM %1$s " +
                        " WHERE %1$s.%3$s ",
                wrapDoubleQuotes(entityRef.getName().toLowerCase()),
                wrapDoubleQuotes(entityRef.getIdEntityField().getName().toLowerCase()),
                fieldName(lkField, true));
        sqlFilter += String.format(sqlOperator, innerFilter);
        return new QueryWithParams(sqlFilter, Map.of(parameterName, parameters));
    }

    private Object resolveArgumentValue(EntityField lkField, String argument) {
        return FieldConverters.getConvertedFieldValue(lkField, argument);
    }
}
