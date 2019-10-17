package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import it.at7.gemini.schema.EntityField;

import static it.at7.gemini.core.persistence.FilterVisitor.FilterVisitorContext;

public interface GeminiTypeFilterVisitor {

    QueryWithParams visit(EntityField field, ComparisonNode node, FilterVisitorContext filterVisitorContext);
}
