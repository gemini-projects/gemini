package it.at7.gemini.dsl.entities;

import java.util.ArrayList;
import java.util.List;

public class RawEntityBuilder {
    private String name;
    private List<RawEntity.Entry> entries = new ArrayList<>();
    private List<String> implementsIntefaces = new ArrayList<>();

    public RawEntityBuilder addName(String name) {
        this.name = name.toUpperCase();
        return this;
    }

    public RawEntityBuilder addEntry(RawEntity.Entry entry) {
        entries.add(entry);
        return this;
    }

    public RawEntityBuilder addImplementsInterface(String implementsName) {
        implementsIntefaces.add(implementsName);
        return this;
    }

    public RawEntity build() {
        return new RawEntity(name, entries, implementsIntefaces);
    }

    public static class EntryBuilder {
        private String type;
        private String name;
        private boolean isLogicalKey;

        public EntryBuilder(String type, String name) {
            this.type = type;
            this.name = name;
            this.isLogicalKey = false;
        }

        public void isLogicalKey() {
            this.isLogicalKey = true;
        }

        public RawEntity.Entry build() {
            return new RawEntity.Entry(type, name, isLogicalKey);
        }
    }
}
