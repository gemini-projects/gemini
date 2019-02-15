package it.at7.gemini.core;

public class FilterContext {
    private final String geminiSearchString;
    private final String persistenceTypeSearchString;

    public FilterContext(String geminiSearchString,
                         String persistenceTypeSearchString) {
        this.geminiSearchString = geminiSearchString;
        this.persistenceTypeSearchString = persistenceTypeSearchString;
    }

    public String getGeminiSearchString() {
        return geminiSearchString;
    }

    public String getPersistenceTypeSearchString() {
        return persistenceTypeSearchString;
    }

    public static class Builder {
        private String searchString;
        private String persistenceTypeSearchString;

        public Builder() {
        }

        public Builder withGeminiSearchString(String searchString) {
            this.searchString = searchString;
            return this;
        }

        public Builder withPersistenceTypeSearchString(String persistenceTypeSearchString) {
            this.persistenceTypeSearchString = persistenceTypeSearchString;
            return this;
        }

        public FilterContext build() {
            return new FilterContext(searchString, persistenceTypeSearchString);
        }
    }

    public static Builder BUILDER() {
        return new Builder();
    }
}
