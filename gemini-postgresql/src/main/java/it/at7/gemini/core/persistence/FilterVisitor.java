package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.*;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.EntityFieldNotFoundException;
import it.at7.gemini.exceptions.EntityMetaFieldNotFoundException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static it.at7.gemini.schema.FieldType.*;

@Service
public class FilterVisitor implements RSQLVisitor<String, Entity> {
    public static final ComparisonOperator LIKE_OPERATOR = new ComparisonOperator("=like=", false);

    private static final GeminiTypeFilterVisitor BASIC_TYPE_FILTER = new BasicTypeFilterVisitor();
    private static final GeminiTypeFilterVisitor ENTITY_REF_TYPE_FILTER = new EntityRefTypeFilterVisitor();

    private Set<ComparisonOperator> comparisonOperators;

    Map<FieldType, GeminiTypeFilterVisitor> geminiTypeVisitors = Map.of(
            TEXT, BASIC_TYPE_FILTER,
            LONG, BASIC_TYPE_FILTER,
            DOUBLE, BASIC_TYPE_FILTER,
            NUMBER, BASIC_TYPE_FILTER,
            ENTITY_REF, ENTITY_REF_TYPE_FILTER
    );

    @Autowired
    public FilterVisitor() {
        comparisonOperators = RSQLOperators.defaultOperators();
        comparisonOperators.add(LIKE_OPERATOR);
    }

    public Set<ComparisonOperator> getOperators() {
        return comparisonOperators;
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
            EntityField field = resolveEntityField(entity, selector);
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


    private EntityField resolveEntityField(Entity entity, String selector) throws EntityFieldNotFoundException {
        /**
         * try to resolve first of all data fields... then meta fields..
         * and then perform some logic for complex fields
         */
        try {
            return entity.getField(selector);
        } catch (EntityFieldNotFoundException e) {
            try {
                return entity.getMetaField(selector);
            } catch (EntityMetaFieldNotFoundException ex) {
                throw EntityFieldException.ENTITYFIELD_NOT_FOUND(entity, selector);
            }
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
