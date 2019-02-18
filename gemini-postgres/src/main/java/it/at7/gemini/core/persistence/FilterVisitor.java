package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.*;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilterVisitor implements RSQLVisitor<String, Entity> {

    private GeminiTypeFilterVisitor geminiTypeFileterVisitor;

    @Autowired
    public FilterVisitor(GeminiTypeFilterVisitor geminiTypeFileterVisitor) {
        this.geminiTypeFileterVisitor = geminiTypeFileterVisitor;
    }

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
            return geminiTypeFileterVisitor.visit(field, node);
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
