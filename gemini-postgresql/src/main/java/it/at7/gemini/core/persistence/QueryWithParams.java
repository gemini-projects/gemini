package it.at7.gemini.core.persistence;

import java.util.HashMap;
import java.util.Map;

public class QueryWithParams {
    private StringBuilder sqlBuilder;
    private Map<String, Object> params;

    public QueryWithParams(String sql) {
        this.sqlBuilder = new StringBuilder(sql);
        this.params = new HashMap<>();
    }

    public QueryWithParams(String sql, Map<String, Object> params) {
        this.sqlBuilder = new StringBuilder(sql);
        this.params = params;
    }

    public QueryWithParams addToSql(String sql) {
        sqlBuilder.append(sql);
        return this;
    }

    public QueryWithParams addParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public String getSql() {
        return sqlBuilder.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
