package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static cz.jirutka.rsql.parser.ast.RSQLOperators.*;

@Service
public class BasicFieldTypeFilterVisitor {

    Map<ComparisonOperator, String> operatorsMapping = Map.of(
            EQUAL, " = ",
            NOT_EQUAL, "!=",
            GREATER_THAN, ">",
            GREATER_THAN_OR_EQUAL, ">=",
            LESS_THAN, "<",
            LESS_THAN_OR_EQUAL, "<="
    );

    public String visit(EntityField field, ComparisonNode node) {
        ComparisonOperator operator = node.getOperator();
        List<String> arguments = node.getArguments();
        String argument = arguments.get(0);// always true
        String stOperator = fromOperatorToCompareString(operator);
        if (stOperator == null)
            return "";
        FieldType type = field.getType();
        if(type.equals(FieldType.TEXT)){
            argument = "'" + argument + "'";
        }
        return field.getName().toLowerCase() + stOperator + argument;

    }

    private String fromOperatorToCompareString(ComparisonOperator operator) {
        String op = operatorsMapping.get(operator);
        return op == null ? String.format(" 1 = 0 /* not supported %s */", operator.getSymbol()) : op;
    }
}
