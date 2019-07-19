package it.at7.gemini.dsl.entities;

import java.util.ArrayList;
import java.util.List;

public class RawEntityBuilder {
    private static final String namePattern = "^[a-zA-Z0-9_]{3,}$";

    private String name;
    private boolean embedable = false;
    private boolean oneRecord = false;
    private List<RawEntity.Entry> entries = new ArrayList<>();
    private List<String> implementsIntefaces = new ArrayList<>();

    public RawEntityBuilder addName(String name) {
        if (!name.matches(namePattern)) {
            throw new RuntimeException(String.format("name %s doesn't match regexp ^[a-zA-Z]{3,}$", name));
        }
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

    public RawEntityBuilder isEmbedable() {
        this.embedable = true;
        return this;
    }

    public RawEntityBuilder isOneRec() {
        assert !this.embedable;
        this.oneRecord = true;
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean getIsOneRec() {
        return this.oneRecord;
    }

    public RawEntity build() {
        return new RawEntity(name, embedable, oneRecord, entries, implementsIntefaces);
    }

    public static class EntryBuilder {
        private final RawEntityBuilder entityBuilder;
        private String type;
        private String name;
        private boolean isLogicalKey;

        public EntryBuilder(RawEntityBuilder entityBuilder, String type, String name) {
            this.entityBuilder = entityBuilder;
            this.type = type;
            if (!name.matches(namePattern)) {
                throw new RuntimeException(String.format("name %s doesn't match regexp ^[a-zA-Z0-9_]{3,}$", name));
            }
            this.name = name;
            this.isLogicalKey = false;
        }

        public RawEntityBuilder getEntityBuilder() {
            return entityBuilder;
        }

        public void isLogicalKey() {
            this.isLogicalKey = true;
        }

        public RawEntity.Entry build() {
            return new RawEntity.Entry(type, name, isLogicalKey);
        }
    }
}
