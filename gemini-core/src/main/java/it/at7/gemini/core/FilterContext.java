package it.at7.gemini.core;

public class FilterContext {
    private String searchString;
    private FilterType filterType;
    private int limit;
    private final int start;
    private final String[] orderBy;

    public FilterContext(FilterType filterType, String searchString, int limit, int start, String[] orderBy) {
        this.filterType = filterType;
        this.searchString = searchString;
        this.limit = limit;
        this.start = start;
        this.orderBy = orderBy;
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

    // ============ static binding to builder ===========

    public static FilterContext withGeminiSearchString(String searchString){
        return new FilterContextBuilder().withGeminiSearchString(searchString).build();
    }

    public static FilterContext ALL = new FilterContextBuilder().build();

    public enum FilterType {
        GEMINI,
        PERSISTENCE
    }
}
