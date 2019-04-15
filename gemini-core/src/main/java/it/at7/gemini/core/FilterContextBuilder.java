package it.at7.gemini.core;

import java.util.Map;

public class FilterContextBuilder {
    public static final String SEARCH_PARAMETER = "search";
    public static final String LIMIT_PARAMETER = "limit";
    public static final String START_PARAMETER = "start";

    private final ConfigurationService configurationService;

    private String searchString;
    private FilterContext.FilterType filterType;
    private int limit;
    private int start;

    public FilterContextBuilder() {
        this.configurationService = null;
    }

    public FilterContextBuilder(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public FilterContextBuilder fromParameters(Map<String, String[]> parameters) {
        withGeminiSearchString(getSearchFromParameters(parameters.get(SEARCH_PARAMETER)));
        withLimit(getLimitFromParameters(parameters.get(LIMIT_PARAMETER)));
        withStart(getStartFromParameters(parameters.get(START_PARAMETER)));
        return this;
    }

    public FilterContextBuilder withGeminiSearchString(String searchString) {
        this.filterType = FilterContext.FilterType.GEMINI;
        this.searchString = searchString;
        return this;
    }

    public FilterContextBuilder withLimit(int limit) {
        this.limit = limit;
        return this;
    }

    private FilterContextBuilder withStart(int start) {
        this.start = start;
        return this;
    }

    public FilterContextBuilder withPersistenceTypeSearchString(String searchString) {
        this.filterType = FilterContext.FilterType.PERSISTENCE;
        this.searchString = searchString;
        return this;
    }

    public FilterContext build() {
        return new FilterContext(filterType, searchString, limit, start);
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

    private int getStartFromParameters(String[] startParams) {
        if (startParams != null && startParams.length > 0) {
            return Integer.parseInt(startParams[0]); // only the first supporterd
        }
        return 0;
    }
}
