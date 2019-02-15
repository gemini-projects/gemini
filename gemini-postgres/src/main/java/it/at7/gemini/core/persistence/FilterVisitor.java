package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.*;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilterVisitor implements RSQLVisitor<String, Entity> {

    private BasicFieldTypeFilterVisitor basicFieldTypeFilterVisitor;

    @Autowired
    public FilterVisitor(BasicFieldTypeFilterVisitor basicFieldTypeFilterVisitor) {
        this.basicFieldTypeFilterVisitor = basicFieldTypeFilterVisitor;
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
            return visitEntityField(field, node);
        } catch (EntityFieldException e) {
            // TODO strict filter ? pass Context instead of Entity
            return "";
        }
    }

    private String visitEntityField(EntityField field, ComparisonNode node) {
        switch (field.getType()) {
            case PK:
                break;
            case TEXT:
            case NUMBER:
            case LONG:
            case DOUBLE:
            case BOOL:
                return basicFieldTypeFilterVisitor.visit(field, node);
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
        throw new UnsupportedOperationException(String.format("Unsupported filter for %s", field.getType()));
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
