package it.at7.gemini.dsl.entities;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RawEntity {
    private final String name;
    private final boolean embedable;
    private final boolean oneRecord;
    private final boolean tree;
    private final List<Entry> entries;
    private final List<String> implementsIntefaces;

    public RawEntity(String name, boolean embedable, boolean oneRecord, boolean tree, List<Entry> entries, List<String> implementsIntefaces) {
        this.name = name;
        this.embedable = embedable;
        this.oneRecord = oneRecord;
        this.tree = tree;
        this.entries = Collections.unmodifiableList(entries);
        this.implementsIntefaces = Collections.unmodifiableList(implementsIntefaces);
    }

    public String getName() {
        return name;
    }

    public boolean isEmbedable() {
        return embedable;
    }

    public boolean isOneRecord() {
        return oneRecord;
    }

    public boolean isTree() {
        return tree;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public List<String> getImplementsIntefaces() {
        return implementsIntefaces;
    }

    @Override
    public String toString() {
        StringBuilder stB = new StringBuilder();
        stB.append("ENTITY ");
        stB.append(name);
        if (!implementsIntefaces.isEmpty()) {
            stB.append(" IMPLEMENTS ");
            implementsIntefaces.forEach(i -> {
                stB.append("\t");
                stB.append(i);
            });
        }
        stB.append(" {\n");
        entries.forEach(e -> {
            stB.append("\t");
            stB.append(e.toString());
            stB.append("\n");
        });
        stB.append("}");
        return stB.toString();
    }

    public static class Entry {
        private final String type;
        private final String name;
        private final boolean isLogicalKey;
        private final int lkOrder;

        public Entry(String type, String name, boolean isLogicalKey, int lkOrder) {
            this.type = type;
            this.name = name;
            this.isLogicalKey = isLogicalKey;
            this.lkOrder = lkOrder;
            assert !isLogicalKey || lkOrder > 0;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public boolean isLogicalKey() {
            return isLogicalKey;
        }

        public int getLkOrder() {
            return lkOrder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry entry = (Entry) o;
            return isLogicalKey == entry.isLogicalKey &&
                    lkOrder == entry.lkOrder &&
                    type.equals(entry.type) &&
                    name.equals(entry.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, isLogicalKey, lkOrder);
        }

        @Override
        public String toString() {
            return type + "\t\t" + name + (isLogicalKey ? (" *" + lkOrder) : "");
        }
    }
}
