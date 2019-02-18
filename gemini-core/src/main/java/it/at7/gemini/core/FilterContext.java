package it.at7.gemini.core;

public class FilterContext {
    private final String searchString;
    private final SearchType searchType;

    public FilterContext(SearchType searchType, String searchString) {
        this.searchType = searchType;
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    // ============ static binding to builder ===========

    public static FilterContext withGeminiSearchString(String searchString){
        return new Builder().withGeminiSearchString(searchString).build();
    }


    // ======================

    public enum SearchType {
        GEMINI,
        PERSISTENCE
    }

    // ============== BUILDER ========== //

    public static class Builder {
        private String searchString;
        private SearchType searchType;

        public Builder() {
        }

        public Builder withGeminiSearchString(String searchString) {
            this.searchType = SearchType.GEMINI;
            this.searchString = searchString;
            return this;
        }

        public Builder withPersistenceTypeSearchString(String searchString) {
            this.searchType = SearchType.PERSISTENCE;
            this.searchString = searchString;
            return this;
        }

        public FilterContext build() {
            return new FilterContext(searchType, searchString);
        }
    }

    public static Builder BUILDER() {
        return new Builder();
    }

}
