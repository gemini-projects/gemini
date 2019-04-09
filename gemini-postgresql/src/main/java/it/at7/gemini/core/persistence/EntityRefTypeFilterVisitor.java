package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;
import static cz.jirutka.rsql.parser.ast.RSQLOperators.NOT_IN;

public class EntityRefTypeFilterVisitor implements GeminiTypeFilterVisitor {

    Map<ComparisonOperator, String> supportedOperators = Map.of(
            EQUAL, " IN ",
            NOT_EQUAL, "NOT IN",
            IN, " IN ",
            NOT_IN, " NOT IN "
    );

    @Override
    public String visit(EntityField field, ComparisonNode node) {
        ComparisonOperator operator = node.getOperator();
        if (supportedOperators.containsKey(operator)) {
            FieldType type = field.getType();
            assert type.equals(FieldType.ENTITY_REF);
            String sqlOperator = supportedOperators.get(operator);
            Optional<String> singleLKsql = handleSingleLogicalKeyEntity(field, node, sqlOperator);
            if (singleLKsql.isPresent())
                return field.getName().toLowerCase() + " " + sqlOperator + " ( " + singleLKsql.get() + " ) ";
            throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor TODO multiple field logical key", node.getOperator().getSymbol()));
        }
        throw new GeminiRuntimeException(String.format("EntityRefTypeFilterVisitor unsupported operator %s", node.getOperator().getSymbol()));
    }

    private Optional<String> handleSingleLogicalKeyEntity(EntityField field, ComparisonNode node, String sqlOperator) {
        Entity entityRef = field.getEntityRef();
        Entity.LogicalKey logicalKey = entityRef.getLogicalKey();
        if (logicalKey.getLogicalKeyList().size() == 1) {
            EntityField lkField = logicalKey.getLogicalKeyList().get(0);
            String sqlFilter = String.format("" +
                    "SELECT %1$s.%2$s" +
                    "  FROM %1$s " +
                    " WHERE %1$s.%3$s IN ( ", entityRef.getName().toLowerCase(), entityRef.getIdEntityField().getName().toLowerCase(), lkField.getName().toLowerCase());
            List<String> arguments = node.getArguments();
            for (int i = 0; i < arguments.size(); i++) {
                sqlFilter += resolveArgumentValue(lkField, arguments.get(i));
                if (i != arguments.size() - 1) {
                    sqlFilter += " , ";
                }
            }
            sqlFilter += " )";
            return Optional.of(sqlFilter);
        }
        return Optional.empty();
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
            case TRANSL_TEXT:
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
