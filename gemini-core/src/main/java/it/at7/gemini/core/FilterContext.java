package it.at7.gemini.core;

public class FilterContext {
    private String searchString;
    private FilterType filterType;
    private int limit;
    private final int start;

    public FilterContext(FilterType filterType, String searchString, int limit, int start) {
        this.filterType = filterType;
        this.searchString = searchString;
        this.limit = limit;
        this.start = start;
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

    // ============ static binding to builder ===========

    public static FilterContext withGeminiSearchString(String searchString){
        return new FilterContextBuilder().withGeminiSearchString(searchString).build();
    }

    public enum FilterType {
        GEMINI,
        PERSISTENCE
    }
}
