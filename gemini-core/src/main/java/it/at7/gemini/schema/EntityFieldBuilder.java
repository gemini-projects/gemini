package it.at7.gemini.schema;

public class EntityFieldBuilder {
    private Entity entity;
    private FieldType fieldType;
    private String name;
    private boolean logicalKey;
    private String refEntityName;
    // private String refEntityCollectionFieldName;

    public EntityFieldBuilder(FieldType fieldType, String name, boolean logicalKey, String refEntityName/*, String refEntityCollectionFieldName*/) {
        this.fieldType = fieldType;
        this.name = name;
        this.logicalKey = logicalKey;
        this.refEntityName = refEntityName;
        // this.refEntityCollectionFieldName = refEntityCollectionFieldName;
    }

    public EntityFieldBuilder setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public EntityField build() {
        return new EntityField(entity, fieldType, name, logicalKey, refEntityName/* , refEntityCollectionFieldName*/);
    }

    public static EntityField ID(Entity entity) {
        return new EntityField(entity, FieldType.PK, Field.ID_NAME, false, null);
    }

}
