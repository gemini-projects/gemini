package it.at7.gemini.schema;

public class EntityFieldBuilder {
    private Entity entity;
    private FieldType fieldType;
    private String name;
    private boolean logicalKey;
    private String refEntityName;
    private final String interfaceName;
    private final EntityField.Scope scope;

    public EntityFieldBuilder(FieldType fieldType, String name, boolean logicalKey, String refEntityName, String interfaceName, EntityField.Scope scope) {
        this.fieldType = fieldType;
        this.name = name;
        this.logicalKey = logicalKey;
        this.refEntityName = refEntityName;
        this.scope = scope;
        this.interfaceName = interfaceName; // field is defined in this interface -- if null no interface its an entityfield
    }

    public EntityFieldBuilder setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public EntityField.Scope getScope() {
        return scope;
    }

    public EntityField build() {
        return new EntityField(entity, fieldType, name, logicalKey, refEntityName, scope);
    }

    public static EntityField ID(Entity entity) {
        return new EntityField(entity, FieldType.PK, Field.ID_NAME, false, null, EntityField.Scope.DATA);
    }

}
