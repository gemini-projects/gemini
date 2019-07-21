package it.at7.gemini.schema;

import it.at7.gemini.core.Module;
import it.at7.gemini.dsl.entities.RawEntity;

import java.util.ArrayList;
import java.util.List;

public class EntityBuilder {
    private final Module mainModule;
    private final RawEntity rawEntity;
    private final String entityName;
    private final List<EntityFieldBuilder> fieldsBuilders = new ArrayList<>();
    private Object defaultRecord;
    private List<ExtraEntity> extraEntities = new ArrayList<>();


    public EntityBuilder(RawEntity rawEntity, Module module) {
        this.rawEntity = rawEntity;
        this.entityName = rawEntity.getName();
        this.mainModule = module;

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

    public void addExtraEntity(RawEntity rawEntity, Module module) {
        extraEntities.add(new ExtraEntity(rawEntity, module));
    }

    public Entity build() {
        return new Entity(mainModule, entityName, rawEntity.isEmbedable(), rawEntity.isOneRecord(), fieldsBuilders, defaultRecord);
    }

    public List<ExtraEntity> getExternalEntities() {
        return extraEntities;
    }

    public static class ExtraEntity {
        RawEntity rawEntity;
        Module module;

        ExtraEntity(RawEntity rawEntity, Module module) {
            this.rawEntity = rawEntity;
            this.module = module;
        }

        public RawEntity getRawEntity() {
            return rawEntity;
        }

        public Module getModule() {
            return module;
        }
    }

}
