package it.at7.gemini.core;

public class FilterContext {
    private String searchString;
    private SearchType searchType;
    private int limit;

    public FilterContext(SearchType searchType, int limit) {
        this.searchString = "";
        this.searchType = searchType;
        this.limit = limit;
    }

    public FilterContext(SearchType searchType, String searchString, int limit) {
        this.searchType = searchType;
        this.searchString = searchString;
        this.limit = limit;
    }

    public String getSearchString() {
        return searchString;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public int getLimit() {
        return limit;
    }

    // ============ static binding to builder ===========

    public static FilterContext withGeminiSearchString(String searchString){
        return new SearchContextBuilder().withGeminiSearchString(searchString).build();
    }

    public enum SearchType {
        GEMINI,
        PERSISTENCE
    }
}
