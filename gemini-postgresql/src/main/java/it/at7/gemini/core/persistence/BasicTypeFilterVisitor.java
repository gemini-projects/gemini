package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import it.at7.gemini.core.FieldConverters;
import it.at7.gemini.core.persistence.FilterVisitor.FilterVisitorContext;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.EntityField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;
import static it.at7.gemini.core.persistence.FieldTypePersistenceUtility.fieldName;


public class BasicTypeFilterVisitor implements GeminiTypeFilterVisitor {

    Map<ComparisonOperator, String> singleArgumentOpertors = Map.of(
            EQUAL, " = ",
            NOT_EQUAL, " != ",
            GREATER_THAN, " > ",
            GREATER_THAN_OR_EQUAL, " >= ",
            LESS_THAN, " < ",
            LESS_THAN_OR_EQUAL, " <= ",
            FilterVisitor.LIKE_OPERATOR, " LIKE "
    );
    Map<ComparisonOperator, String> multipleArgumentsOperators = Map.of(
            IN, " IN ",
            NOT_IN, " NOT IN "
    );

    public QueryWithParams visit(EntityField field, ComparisonNode node, FilterVisitorContext filterVisitorContext) {
        ComparisonOperator operator = node.getOperator();
        List<String> arguments = node.getArguments();
        QueryWithParams singleArg = handleSingleArgumentOperator(field, operator, arguments, filterVisitorContext);
        if (singleArg != null)
            return singleArg;
        QueryWithParams multipleArg = handleMultipleArgumentOperator(field, operator, arguments, filterVisitorContext);
        if (multipleArg == null) {
            throw new GeminiRuntimeException(String.format("Filter not impemented for type %s", field.getType()));
        }
        return multipleArg;
    }

    private QueryWithParams handleSingleArgumentOperator(EntityField field, ComparisonOperator operator, List<String> arguments, FilterVisitorContext filterVisitorContext) {
        String argument = arguments.get(0);// always true
        String stOperator = singleArgumentOpertors.get(operator);
        if (stOperator == null)
            return null;
        Object resArgument = handleStringValueForField(field, argument);
        String parameterName = filterVisitorContext.parameterFor(fieldName(field, false));
        String sql = String.format(" \"%s\".\"%s\" %s :%s ", field.getEntity().getName().toLowerCase(), fieldName(field, false), stOperator, parameterName);
        return new QueryWithParams(sql, Map.of(parameterName, resArgument));
    }

    private QueryWithParams handleMultipleArgumentOperator(EntityField field, ComparisonOperator operator, List<String> arguments, FilterVisitorContext filterVisitorContext) {
        String stOperator = multipleArgumentsOperators.get(operator);
        if (stOperator == null) {
            return null;
        }
        List<Object> parameters = arguments.stream().map(a -> handleStringValueForField(field, a)).collect(Collectors.toList());
        String parameterName = filterVisitorContext.parameterFor(fieldName(field, false));

        String sql = String.format(" \"%s\".\"%s\" %s (:%s) ", field.getEntity().getName().toLowerCase(), fieldName(field, false), stOperator, parameterName);
        return new QueryWithParams(sql, Map.of(parameterName, parameters));
    }

    private Object handleStringValueForField(EntityField field, String argument) {
        return FieldConverters.getConvertedFieldValue(field, argument);
    }
}
