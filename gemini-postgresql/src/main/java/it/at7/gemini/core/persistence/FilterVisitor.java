package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.*;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;
import org.springframework.stereotype.Service;

import java.util.Map;

import static it.at7.gemini.schema.FieldType.*;

@Service
public class FilterVisitor implements RSQLVisitor<String, Entity> {
    private static final GeminiTypeFilterVisitor BASIC_TYPE_FILTER = new BasicTypeFilterVisitor();
    private static final GeminiTypeFilterVisitor ENTITY_REF_TYPE_FILTER = new EntityRefTypeFilterVisitor();


    Map<FieldType, GeminiTypeFilterVisitor> geminiTypeVisitors = Map.of(
            TEXT, BASIC_TYPE_FILTER,
            LONG, BASIC_TYPE_FILTER,
            DOUBLE, BASIC_TYPE_FILTER,
            NUMBER, BASIC_TYPE_FILTER,
            ENTITY_REF, ENTITY_REF_TYPE_FILTER
    );

    @Override
    public String visit(AndNode node, Entity entity) {
        return iterateAndApplyOperator("AND", node, entity);
    }

    @Override
    public String visit(OrNode node, Entity entity) {
        return iterateAndApplyOperator("OR", node, entity);
    }

    @Override
    public String visit(ComparisonNode node, Entity entity) {
        String selector = node.getSelector();
        try {
            EntityField field = entity.getField(selector);
            GeminiTypeFilterVisitor geminiTypeFilterVisitor = geminiTypeVisitors.get(field.getType());
            if (geminiTypeFilterVisitor == null) {
                throw new GeminiRuntimeException(String.format("Filter Not Implemented for type %s", field.getType()));
            }
            return geminiTypeFilterVisitor.visit(field, node);
        } catch (EntityFieldException e) {
            // TODO strict filter ? pass Context instead of Entity
            throw new GeminiRuntimeException("Filter Not Supported", e);
        }
    }

    private String iterateAndApplyOperator(String operator, LogicalNode node, Entity entity) {
        String sql = "( ";
        boolean first = true;
        for (Node child : node.getChildren()) {
            if (!first) {
                sql += " " + operator + " ";
            }
            first = false;
            sql += child.accept(this, entity);
        }
        sql += " )";
        return sql;
    }
}
