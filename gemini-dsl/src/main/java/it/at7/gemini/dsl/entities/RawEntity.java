package it.at7.gemini.dsl.entities;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RawEntity {
    private final String name;
    private final List<Entry> entries;
    private List<String> implementsIntefaces;

    public RawEntity(String name, List<Entry> entries, List<String> implementsIntefaces) {
        this.name = name;
        this.entries = Collections.unmodifiableList(entries);
        this.implementsIntefaces = Collections.unmodifiableList(implementsIntefaces);
    }

    public String getName() {
        return name;
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
        if(!implementsIntefaces.isEmpty()){
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

        public Entry(String type, String name, boolean isLogicalKey) {
            this.type = type;
            this.name = name;
            this.isLogicalKey = isLogicalKey;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return isLogicalKey == entry.isLogicalKey &&
                    Objects.equals(type, entry.type) &&
                    Objects.equals(name, entry.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name, isLogicalKey);
        }

        @Override
        public String toString() {
            return type + "\t\t" + name + (isLogicalKey ? " *" : "");
        }
    }
}
