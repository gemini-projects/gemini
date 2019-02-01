package it.at7.gemini.core;

public class FilterRequest {
    private final String searchString;

    public FilterRequest(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public static class Builder {
        private String searchString;

        public Builder() {
        }

        public Builder with(String searchString) {
            this.searchString = searchString;
            return this;
        }

        public FilterRequest build() {
            return new FilterRequest(searchString);
        }
    }

    public static Builder BUILDER() {
        return new Builder();
    }
}
