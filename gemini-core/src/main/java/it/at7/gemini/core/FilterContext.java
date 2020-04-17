package it.at7.gemini.core;

import java.util.Map;

public class FilterContext extends BasicFilterContext {
    private String searchString;
    private FilterType filterType;
    private final boolean count;
    private final Map<String, Object> params;

    public FilterContext(FilterType filterType, String searchString, int limit, int start, String[] orderBy, boolean count, Map<String, Object> params) {
        super(limit, start, orderBy);
        this.filterType = filterType;
        this.searchString = searchString;
        this.count = count;
        this.params = params;
    }

    public String getSearchString() {
        return searchString;
    }

    public FilterType getFilterType() {
        return filterType;
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
