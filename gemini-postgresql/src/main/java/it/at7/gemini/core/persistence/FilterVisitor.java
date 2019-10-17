package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.*;
import it.at7.gemini.exceptions.EntityFieldException;
import it.at7.gemini.exceptions.EntityFieldNotFoundException;
import it.at7.gemini.exceptions.EntityMetaFieldNotFoundException;
import it.at7.gemini.exceptions.GeminiRuntimeException;
import it.at7.gemini.schema.Entity;
import it.at7.gemini.schema.EntityField;
import it.at7.gemini.schema.FieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static it.at7.gemini.schema.FieldType.*;

public class FilterVisitor implements RSQLVisitor<QueryWithParams, FilterVisitor.FilterVisitorContext> {
    public static final ComparisonOperator LIKE_OPERATOR = new ComparisonOperator("=like=", false);


    private Set<ComparisonOperator> comparisonOperators;

    private final GeminiTypeFilterVisitor BASIC_TYPE_FILTER;
    private final GeminiTypeFilterVisitor ENTITY_REF_TYPE_FILTER;
    private final Map<FieldType, GeminiTypeFilterVisitor> geminiTypeVisitors;

    public FilterVisitor() {
        comparisonOperators = RSQLOperators.defaultOperators();
        comparisonOperators.add(LIKE_OPERATOR);
        BASIC_TYPE_FILTER = new BasicTypeFilterVisitor();
        ENTITY_REF_TYPE_FILTER = new EntityRefTypeFilterVisitor(this);
        geminiTypeVisitors = Map.of(
                TEXT, BASIC_TYPE_FILTER,
                LONG, BASIC_TYPE_FILTER,
                BOOL, BASIC_TYPE_FILTER,
                DOUBLE, BASIC_TYPE_FILTER,
                NUMBER, BASIC_TYPE_FILTER,
                DATE, BASIC_TYPE_FILTER,
                DATETIME, BASIC_TYPE_FILTER,
                TIME, BASIC_TYPE_FILTER,
                ENTITY_REF, ENTITY_REF_TYPE_FILTER
        );
    }

    public Set<ComparisonOperator> getOperators() {
        return comparisonOperators;
    }

    @Override
    public QueryWithParams visit(AndNode node, FilterVisitorContext filterVisitorContext) {
        return iterateAndApplyOperator("AND", node, filterVisitorContext);
    }

    @Override
    public QueryWithParams visit(OrNode node, FilterVisitorContext filterVisitorContext) {
        return iterateAndApplyOperator("OR", node, filterVisitorContext);
    }

    @Override
    public QueryWithParams visit(ComparisonNode node, FilterVisitorContext filterVisitorContext) {
        String selector = node.getSelector();
        try {
            EntityField field = resolveEntityField(filterVisitorContext.entity, selector);
            GeminiTypeFilterVisitor geminiTypeFilterVisitor = geminiTypeVisitors.get(field.getType());
            if (geminiTypeFilterVisitor == null) {
                throw new GeminiRuntimeException(String.format("Filter Not Implemented for type %s", field.getType()));
            }
            return geminiTypeFilterVisitor.visit(field, node, filterVisitorContext);
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


    private QueryWithParams iterateAndApplyOperator(String operator, LogicalNode node, FilterVisitorContext filterVisitorContext) {
        QueryWithParams q = new QueryWithParams("(");
        boolean first = true;
        for (Node child : node.getChildren()) {
            if (!first) {
                q.addToSql(" " + operator + " ");
            }
            first = false;
            QueryWithParams childQP = child.accept(this, filterVisitorContext);
            q.addToSql(childQP.getSql());
            q.addParams(childQP.getParams());

        }
        q.addToSql(" )");
        return q;
    }


    public static class FilterVisitorContext {
        Entity entity;

        /**
         * Used to store the index for the same parameter name resolution
         */

        Map<String, Long> counterByParameter;

        public FilterVisitorContext(Entity entity) {
            this.entity = entity;
            counterByParameter = new HashMap<>();
        }

        public FilterVisitorContext(Entity entity, Map<String, Long> counterByParameter) {
            this.entity = entity;
            this.counterByParameter = counterByParameter;
        }

        public static FilterVisitorContext of(Entity entity) {
            return new FilterVisitorContext(entity);
        }

        protected static FilterVisitorContext of(Entity entity, Map<String, Long> counterByParameter) {
            return new FilterVisitorContext(entity, counterByParameter);
        }

        public String parameterFor(String fieldName) {
            Long index = counterByParameter.compute(fieldName, (k, v) -> v == null ? 1 : (v + 1));
            return fieldName + "_" + index;
        }
    }
}
