package it.at7.gemini.core;

public class FilterContext {
    private String searchString;
    private FilterType filterType;
    private int limit;

    public FilterContext(FilterType filterType, int limit) {
        this.searchString = "";
        this.filterType = filterType;
        this.limit = limit;
    }

    public FilterContext(FilterType filterType, String searchString, int limit) {
        this.filterType = filterType;
        this.searchString = searchString;
        this.limit = limit;
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

    // ============ static binding to builder ===========

    public static FilterContext withGeminiSearchString(String searchString){
        return new FilterContextBuilder().withGeminiSearchString(searchString).build();
    }

    public enum FilterType {
        GEMINI,
        PERSISTENCE
    }
}
