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

    public EntityBuilder addField(FieldType fieldType, RawEntity.Entry entry, String interfaceName, EntityField.Scope scope) {
        return addField(fieldType, entry, null, interfaceName, scope);
    }

    public EntityBuilder addField(FieldType fieldType, RawEntity.Entry entry, String refEntityName, String interfaceName, EntityField.Scope scope) {
        fieldsBuilders.add(new EntityFieldBuilder(fieldType, entry.getName(), entry.isLogicalKey(), refEntityName, interfaceName, scope));
        return this;
    }

    public EntityBuilder setDefaultRecord(Object defRecord) {
        this.defaultRecord = defRecord;
        return this;
    }

    public Entity build() {
        return new Entity(module, entityName, rawEntity.isEmbedable(), fieldsBuilders, defaultRecord);
    }
}
