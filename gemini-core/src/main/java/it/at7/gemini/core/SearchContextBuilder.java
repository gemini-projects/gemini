package it.at7.gemini.core;

import java.util.Map;

public class SearchContextBuilder {
    public static final String SEARCH_PARAMETER = "search";
    private static final String LIMIT_PARAMETER = "limit";

    private final ConfigurationService configurationService;

    private String searchString;
    private FilterContext.SearchType searchType;
    private int limit;

    public SearchContextBuilder() {
        this.configurationService = null;
    }

    public SearchContextBuilder(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public SearchContextBuilder fromParameters(Map<String, String[]> parameters) {
        withGeminiSearchString(getSearchFromParameters(parameters.get(SEARCH_PARAMETER)));
        withLimit(getLimitFromParameters(parameters.get(LIMIT_PARAMETER)));
        return this;
    }

    public SearchContextBuilder withGeminiSearchString(String searchString) {
        this.searchType = FilterContext.SearchType.GEMINI;
        this.searchString = searchString;
        return this;
    }

    public SearchContextBuilder withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public SearchContextBuilder withPersistenceTypeSearchString(String searchString) {
        this.searchType = FilterContext.SearchType.PERSISTENCE;
        this.searchString = searchString;
        return this;
    }

    public FilterContext build() {
        return new FilterContext(searchType, searchString, limit);
    }

    private int getLimitFromParameters(String[] limitParams) {
        if (limitParams != null && limitParams.length > 0) {
            return Integer.parseInt(limitParams[0]); // only the first supporterd
        }
        return configurationService == null ? 0 : configurationService.getApiListLimit();
    }

    private String getSearchFromParameters(String[] searchParams) {
        if (searchParams != null && searchParams.length > 0) {
            return searchParams[0]; // only the first supporterd
        }
        return "";
    }
}
