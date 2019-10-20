package it.at7.gemini.core;

import java.util.Map;

public class FilterContext {
    private String searchString;
    private FilterType filterType;
    private int limit;
    private final int start;
    private final String[] orderBy;
    private final boolean count;
    private final Map<String, Object> params;

    public FilterContext(FilterType filterType, String searchString, int limit, int start, String[] orderBy, boolean count, Map<String, Object> params) {
        this.filterType = filterType;
        this.searchString = searchString;
        this.limit = limit;
        this.start = start;
        this.orderBy = orderBy;
        this.count = count;
        this.params = params;
    }

    public String getSearchString() {
        return searchString;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public int getLimit() {
        return limit;
    }

    public int getStart() {
        return start;
    }

    public String[] getOrderBy() {
        return orderBy;
    }

    public boolean isCount() {
        return count;
    }

    public Map<String, Object> getParams() {
        return params;
    }

// ============ static binding to builder ===========

    public static FilterContext withGeminiSearchString(String searchString) {
        return new FilterContextBuilder().withGeminiSearchString(searchString).build();
    }

    public static FilterContext withPersistenceQueryParam(String condition, Map<String, Object> params) {
        return new FilterContextBuilder().withPersistenceQueryParam(condition, params).build();
    }

    public static FilterContext ALL = new FilterContextBuilder().build();

    public enum FilterType {
        GEMINI,
        PERSISTENCE
    }
}
