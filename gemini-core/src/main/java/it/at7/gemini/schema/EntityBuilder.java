package it.at7.gemini.schema;

import it.at7.gemini.core.Module;
import it.at7.gemini.dsl.entities.RawEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityBuilder {
    private final Module module;
    private final RawEntity rawEntity;
    private final String entityName;
    private final List<EntityFieldBuilder> fieldsBuilders = new ArrayList<>();
    private Object defaultRecord;


    public EntityBuilder(RawEntity rawEntity, Module module) {
        this.rawEntity = rawEntity;
        this.entityName = rawEntity.getName();
        this.module = module;
    }

    public EntityBuilder(String entityName, Module module) {
        this.rawEntity = null;
        this.entityName = entityName;
        this.module = module;
    }

    public RawEntity getRawEntity() {
        return rawEntity;
    }

    public String getName() {
        return rawEntity.getName().toUpperCase();
    }

    public EntityBuilder addField(FieldType fieldType, RawEntity.Entry entry) {
        return addField(fieldType, entry, null);
    }

    public EntityBuilder addField(FieldType fieldType, RawEntity.Entry entry, String refEntityName) {
        // return addField(fieldType, entry, refEntityName, null);
        fieldsBuilders.add(new EntityFieldBuilder(fieldType, entry.getName(), entry.isLogicalKey(), refEntityName));
        return this;
    }

    /*
    public EntityBuilder addField(FieldType fieldType, RawEntity.Entry entry, String collectionEntityName, String collectionEntityField) {
        fieldsBuilders.add(new EntityFieldBuilder(fieldType, entry.getName(), entry.isLogicalKey(), collectionEntityName, collectionEntityField));
        return this;
    } */

    public EntityBuilder setDefaultRecord(Object defRecord) {
        this.defaultRecord = defRecord;
        return this;
    }

    public Entity build() {
        return new Entity(module, entityName, fieldsBuilders, defaultRecord);
    }
}
