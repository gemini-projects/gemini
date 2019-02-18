package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;

@Service
public class GeminiTypeFilterVisitor {

    Map<ComparisonOperator, String> singleArgumentOpertors = Map.of(
            EQUAL, " = ",
            NOT_EQUAL, "!=",
            GREATER_THAN, ">",
            GREATER_THAN_OR_EQUAL, ">=",
            LESS_THAN, "<",
            LESS_THAN_OR_EQUAL, "<="
    );
    Map<ComparisonOperator, String> multipleArgumentsOperators = Map.of(
            IN, " IN ",
            NOT_IN, " NOT IN "
    );

    public String visit(EntityField field, ComparisonNode node) {
        ComparisonOperator operator = node.getOperator();
        List<String> arguments = node.getArguments();
        String singleArg = handleSingleArgumentOperator(field, operator, arguments);
        if (singleArg != null)
            return singleArg;
        String multipleArg = handleMultipleArgumentOperator(field, operator, arguments);
        if (multipleArg == null) {
            throw new GeminiRuntimeException(String.format("Filter not impemented for type %s", field.getType()));
        }
        return multipleArg;
    }

    private String handleSingleArgumentOperator(EntityField field, ComparisonOperator operator, List<String> arguments) {
        String argument = arguments.get(0);// always true
        String stOperator = singleArgumentOpertors.get(operator);
        if (stOperator == null)
            return null;
        FieldType type = field.getType();
        argument = handleStringValueForType(type, argument);
        return field.getName().toLowerCase() + stOperator + argument;
    }

    private String handleMultipleArgumentOperator(EntityField field, ComparisonOperator operator, List<String> arguments) {
        String stOperator = multipleArgumentsOperators.get(operator);
        if (stOperator == null) {
            return null;
        }
        FieldType type = field.getType();
        String expressionSql = "( ";
        for (int i = 0; i < arguments.size(); i++) {
            String argument = handleStringValueForType(type, arguments.get(i));
            expressionSql += argument;
            if (i != arguments.size() - 1)
                expressionSql += " , ";
        }
        expressionSql += " ) ";
        return field.getName().toLowerCase() + stOperator + expressionSql;
    }

    private String handleStringValueForType(FieldType type, String argument) {
        switch (type) {
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
        throw new GeminiRuntimeException(String.format("Filter not supported yet on type %s", type));
    }
}
