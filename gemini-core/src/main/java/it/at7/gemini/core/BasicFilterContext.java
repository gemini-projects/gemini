package it.at7.gemini.core;

public class BasicFilterContext {
    private int limit;
    private final int start;
    private final String[] orderBy;

    public BasicFilterContext(int limit, int start, String[] orderBy) {
        this.limit = limit;
        this.start = start;
        this.orderBy = orderBy;
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
}
