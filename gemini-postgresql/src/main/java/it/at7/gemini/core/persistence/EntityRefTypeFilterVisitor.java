package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;

import java.util.List;
import java.util.Map;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;
import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.wrapDoubleQuotes;

public class EntityRefTypeFilterVisitor implements GeminiTypeFilterVisitor {

    Map<ComparisonOperator, String> supportedOperators = Map.of(
            EQUAL, " IN ( %s )",
            NOT_EQUAL, " NOT IN ( %s )",
            IN, " IN ( %s )",
            NOT_IN, " NOT IN ( %s ) ",
            FilterVisitor.LIKE_OPERATOR, " LIKE %s"
    );

    @Override
    public String visit(EntityField field, ComparisonNode node) {
        ComparisonOperator operator = node.getOperator();
        if (supportedOperators.containsKey(operator)) {
            FieldType type = field.getType();
            assert type.equals(FieldType.ENTITY_REF);
            String sqlOperator = supportedOperators.get(operator);

            Entity entityRef = field.getEntityRef();
            Entity.LogicalKey refLogicalKey = entityRef.getLogicalKey();
            assert refLogicalKey != null;
            String innerINQuery;
            if (refLogicalKey.getLogicalKeyList().size() == 1) {
                innerINQuery = handleSingleLogicalKeyEntity(field, node, sqlOperator);
            } else {
                innerINQuery = handleMultipleLogicalKeyEntities(field, node, sqlOperator);
            }

            if (innerINQuery == null) {
                throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor TODO multiple field logical key", node.getOperator().getSymbol()));
            }
            return wrapDoubleQuotes(field.getEntity().getName().toLowerCase()) + "." +
                    wrapDoubleQuotes(field.getName().toLowerCase())
                    + " IN ( " + innerINQuery + " )";
        }
        throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor unsupported operator %s", node.getOperator().getSymbol()));
    }

    private String handleMultipleLogicalKeyEntities(EntityField field, ComparisonNode node, String sqlOperator) {
        String selector = node.getSelector();
        List<String> arguments = node.getArguments();
        if (arguments.size() == 1) {
            Entity entityRef = field.getEntityRef();
            String argument = arguments.get(0);
            // parse again the argument
            Node rootNode = new RSQLParser().parse(argument);
            String innerQuery = rootNode.accept(new FilterVisitor(), entityRef);
            return String.format("SELECT %1$s.%2$s" +
                    "  FROM %1$s WHERE ", wrapDoubleQuotes(entityRef.getName().toLowerCase()), wrapDoubleQuotes(entityRef.getIdEntityField().getName().toLowerCase()))
                    + innerQuery;
        }
        throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor unsupported operator %s withRecord for that one argument", node.getOperator().getSymbol()));
    }

    private String handleSingleLogicalKeyEntity(EntityField field, ComparisonNode node, String sqlOperator) {
        Entity entityRef = field.getEntityRef();
        Entity.LogicalKey logicalKey = entityRef.getLogicalKey();
        EntityField lkField = logicalKey.getLogicalKeyList().get(0);
        List<String> arguments = node.getArguments();
        StringBuilder innerFilter = new StringBuilder();
        for (int i = 0; i < arguments.size(); i++) {
            innerFilter.append(resolveArgumentValue(lkField, arguments.get(i)));
            if (i != arguments.size() - 1) {
                innerFilter.append(" , ");
            }
        }
        String sqlFilter = String.format("" +
                        "SELECT %1$s.%2$s" +
                        "  FROM %1$s " +
                        " WHERE %1$s.%3$s ",
                wrapDoubleQuotes(entityRef.getName().toLowerCase()),
                wrapDoubleQuotes(entityRef.getIdEntityField().getName().toLowerCase()),
                wrapDoubleQuotes(lkField.getName().toLowerCase()));
        sqlFilter += String.format(sqlOperator, innerFilter);
        return sqlFilter;
    }

    private String resolveArgumentValue(EntityField lkField, String argument) {
        switch (lkField.getType()) {
            case PK:
            case NUMBER:
            case LONG:
            case DOUBLE:
            case BOOL:
                return argument;
            case TEXT:
                return "'" + argument + "'";
            case TIME:
                break;
            case DATE:
                break;
            case DATETIME:
                break;
            case ENTITY_REF:
                break;
            case GENERIC_ENTITY_REF:
                break;
            case TEXT_ARRAY:
                break;
            case RECORD:
                break;
        }
        throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor: Unsupported resolveArgumentValue %s", lkField.getType()));
    }
}
