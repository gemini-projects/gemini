package it.at7.gemini.core.persistence;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import it.at7.gemini.schema.EntityField;

public interface GeminiTypeFilterVisitor {

    String visit(EntityField field, ComparisonNode node);
}
